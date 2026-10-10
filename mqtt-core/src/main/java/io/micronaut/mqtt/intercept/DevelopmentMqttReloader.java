/*
 * Copyright 2017-2026 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.mqtt.intercept;

import io.micronaut.context.BeanContext;
import io.micronaut.context.BeanRegistration;
import io.micronaut.context.Qualifier;
import io.micronaut.context.WatchableBeanContext;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.env.DevelopmentActive;
import io.micronaut.context.event.BeanPreDestroyEvent;
import io.micronaut.context.event.BeanPreDestroyEventListener;
import io.micronaut.context.reload.ClassChange;
import io.micronaut.context.reload.ClassChangeEvent;
import io.micronaut.context.reload.ReloadStrategy;
import io.micronaut.context.watch.BeanDefinitionChange;
import io.micronaut.context.watch.BeanDefinitionWatcher;
import io.micronaut.context.watch.ClassChangeWatcher;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.order.Ordered;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.BeanDefinitionReference;
import io.micronaut.inject.BeanType;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.mqtt.annotation.MqttSubscriber;
import io.micronaut.mqtt.bind.MqttBinder;
import io.micronaut.mqtt.bind.MqttBinderRegistry;
import io.micronaut.mqtt.serdes.MqttPayloadSerDes;
import io.micronaut.mqtt.serdes.MqttPayloadSerDesRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Keeps the MQTT subscriptions, publishers and payload serializers in step with the code in development mode. It
 * exists only in development mode, so nothing of it is on the path of a message received or published. It serves
 * every client module, Paho v3 and v5 and HiveMQ, as they share the subscriber and publisher advice.
 *
 * <ul>
 *     <li>A definition of a subscriber bean, with {@link MqttSubscriber} on the class or on a method, registered or
 *     removed, or a subscriber class changed in place, restarts the subscriptions: the changed subscriber beans are
 *     recreated, then the subscriber advice, which unsubscribes its topics as it is destroyed.</li>
 *     <li>A {@link MqttPayloadSerDes} or {@link MqttBinder} definition, or a publisher client (a
 *     {@code MqttPublisher} of either protocol version), registered or removed, a class of one of them, of a registry,
 *     or of a factory that produces one, changed in place, or a class change applied in place that retires a
 *     classloader, recreates the payload serializer registry, the binder registry and the publisher advice, whose
 *     publisher state is cached by method. The beans that received them, the publisher clients and the subscriber
 *     advice among them, are destroyed with them, as the dependency graph records, and the subscriptions are
 *     restarted.</li>
 *     <li>A class change applied in place that touches none of these recreates nothing.</li>
 * </ul>
 *
 * <p>The context creates a recreated subscriber advice, or one destroyed as a dependent of a recreated bean, again at
 * once, and gives it the {@code Topic} methods it gave it at startup, so that it subscribes again, on the same client,
 * on top of the new beans.</p>
 *
 * <p>The subscriber advice is {@link AutoCloseable}, but nothing calls its {@code close()} as it is destroyed. In
 * development mode, where the client can outlive the advice, in place or kept across a restart, this reloader closes it
 * as it is destroyed, so that a subscription of a retired subscriber is removed before the new advice subscribes
 * afresh, and nothing of the retired subscriber stays reachable from the client. Outside development mode the advice is
 * destroyed as before, with the context, after which the client disconnects.</p>
 *
 * <p>A change that restarts the application is ignored: the new context subscribes again, and the advice of the one it
 * replaces is closed as it stops. The subscriber advice has no way to unsubscribe the topics of one bean, so a change
 * of one subscriber restarts all of them. Each bean is recreated through {@link WatchableBeanContext#recreate(Object)};
 * a context that does not track bean dependencies recreates nothing, and the subscriptions stay as they are until a
 * restart.</p>
 *
 * <p>The watches run after those of other modules. The reloader holds the context only, never an MQTT bean: a bean
 * that received one is a dependent of it, which recreating it would destroy along with its watches.</p>
 *
 * @author graemerocher
 * @since 4.3.0
 */
@Internal
@Context
@DevelopmentActive
final class DevelopmentMqttReloader implements BeanPreDestroyEventListener<AbstractMqttSubscriberAdvice<?>> {

    private static final Logger LOG = LoggerFactory.getLogger(DevelopmentMqttReloader.class);

    /**
     * The beans that cache what they were built from: the payload serializer and binder registries, which hold the
     * beans registered when they were created, and the publisher advice, which caches the publisher state by method.
     */
    private static final List<Class<?>> REGISTRIES = List.of(MqttPayloadSerDesRegistry.class, MqttBinderRegistry.class, AbstractMqttIntroductionAdvice.class);

    /**
     * The types of a class whose change makes the registries stale.
     */
    private static final List<Class<?>> REGISTERED_TYPES = List.of(MqttPayloadSerDes.class, MqttPayloadSerDesRegistry.class, MqttBinder.class, MqttBinderRegistry.class);

    /**
     * The stereotypes of a subscriber, on the class or a method.
     */
    private static final List<Class<? extends Annotation>> LISTENER_STEREOTYPES = List.of(MqttSubscriber.class);

    /**
     * The stereotypes of a publisher client, of either protocol version.
     */
    private static final List<Class<? extends Annotation>> CLIENTS = List.of(io.micronaut.mqtt.annotation.v3.MqttPublisher.class,
        io.micronaut.mqtt.annotation.v5.MqttPublisher.class);

    /**
     * The subscriber beans: a {@link MqttSubscriber} on the class, or on an executable method.
     */
    private static final Qualifier<Object> LISTENERS = new ListenerQualifier();

    /**
     * The beans the registries are built from: the payload serializers, the binders, their registries and the
     * publisher clients.
     */
    private static final Qualifier<Object> REGISTERED = new RegisteredQualifier();

    private final BeanContext beanContext;

    /**
     * @param beanContext The context, watched when it can be
     */
    DevelopmentMqttReloader(BeanContext beanContext) {
        this.beanContext = beanContext;
        if (beanContext instanceof WatchableBeanContext watchable) {
            watchable.definitions().qualifier(LISTENERS).watch(new ListenerDefinitionsWatcher());
            // one watch, so that a bean that is several of these, such as a serializer that is a binder too, restarts once
            watchable.definitions().qualifier(REGISTERED).watch(new RegistryDefinitionsWatcher());
            watchable.classChanges().watch(new ClassWatcher());
        }
    }

    /**
     * Unsubscribes the topics of a subscriber advice as it is destroyed, recreated in place or stopped with its
     * context, so that the client, which can outlive it, keeps no subscription of it.
     *
     * @param event The event
     * @return The advice
     */
    @Override
    public AbstractMqttSubscriberAdvice<?> onPreDestroy(BeanPreDestroyEvent<AbstractMqttSubscriberAdvice<?>> event) {
        AbstractMqttSubscriberAdvice<?> advice = event.getBean();
        try {
            advice.close();
        } catch (Exception e) {
            LOG.warn("Failed to unsubscribe the topics of {}", advice, e);
        }
        return advice;
    }

    private void onClassChange(ClassChangeEvent change) {
        if (change.strategy() == ReloadStrategy.RESTART) {
            // the new context subscribes again, and the advice of the one it replaces is closed as it stops
            return;
        }
        if (!change.retiredLoaders().isEmpty()) {
            restartSubscriptions(true, List.of(), "a reload retired a classloader");
            return;
        }
        boolean registries = false;
        List<String> listeners = new ArrayList<>();
        for (ClassChange classChange : change.changes()) {
            String className = classChange.className();
            Class<?> type = load(className, change.newLoader());
            if (isListener(type) || wasCompiledWith(className, LISTENER_STEREOTYPES)) {
                listeners.add(className);
            }
            if (isRegisteredOrClient(type) || wasCompiledWith(className, CLIENTS) || wasRegistered(className)) {
                registries = true;
            }
        }
        // a change that touches none of these needs nothing: a module that recreated a bean the subscriber advice
        // received destroyed the advice with it, and the context created it again and subscribed again
        if (registries || !listeners.isEmpty()) {
            restartSubscriptions(registries, listeners, registries ? "a payload serializer, a binder or an MQTT publisher changed" : listeners + " changed");
        }
    }

    private static Class<?> load(String className, ClassLoader loader) {
        try {
            return Class.forName(className, false, loader);
        } catch (ClassNotFoundException | LinkageError e) {
            // removed, or not loadable on its own: nothing of the new generation is built from it
            return null;
        }
    }

    private static boolean isListener(Class<?> type) {
        if (type == null) {
            return false;
        }
        if (hasAnnotation(type.getAnnotations(), MqttSubscriber.class)) {
            return true;
        }
        try {
            for (Method method : type.getMethods()) {
                if (hasAnnotation(method.getAnnotations(), MqttSubscriber.class)) {
                    return true;
                }
            }
        } catch (LinkageError e) {
            // a method signature that does not resolve: the class cannot be a working subscriber
        }
        return false;
    }

    private static boolean isRegisteredOrClient(Class<?> type) {
        if (type == null) {
            return false;
        }
        for (Class<?> registeredType : REGISTERED_TYPES) {
            if (registeredType.isAssignableFrom(type)) {
                return true;
            }
        }
        for (Class<? extends Annotation> client : CLIENTS) {
            if (hasAnnotation(type.getAnnotations(), client)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether an annotation is present, or is the stereotype of one that is.
     */
    private static boolean hasAnnotation(Annotation[] annotations, Class<? extends Annotation> wanted) {
        for (Annotation annotation : annotations) {
            Class<? extends Annotation> annotationType = annotation.annotationType();
            if (annotationType == wanted || annotationType.isAnnotationPresent(wanted)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether the class a change replaces carried one of the stereotypes as the context was compiled: a bean
     * definition of it, or of its proxy, has it on the class or an executable method. The references are matched by
     * name, so that only the definitions of that class are loaded, and nothing of a previous class is kept.
     *
     * @param className The changed class
     * @param stereotypes The stereotypes
     * @return Whether one was there
     */
    private boolean wasCompiledWith(String className, List<Class<? extends Annotation>> stereotypes) {
        for (BeanDefinitionReference<?> reference : definitionsOf(className)) {
            try {
                for (Class<? extends Annotation> stereotype : stereotypes) {
                    if (reference.getAnnotationMetadata().hasStereotype(stereotype)) {
                        return true;
                    }
                }
                BeanDefinition<?> definition = reference.load();
                if (definition == null) {
                    continue;
                }
                for (ExecutableMethod<?, ?> method : definition.getExecutableMethods()) {
                    for (Class<? extends Annotation> stereotype : stereotypes) {
                        if (method.getAnnotationMetadata().hasStereotype(stereotype)) {
                            return true;
                        }
                    }
                }
            } catch (RuntimeException | LinkageError e) {
                // a definition of that name that no longer loads: what it was is unknown, so it counts
                return true;
            }
        }
        return false;
    }

    /**
     * Whether the class a change replaces was a payload serializer, a binder, a registry or a publisher client bean as
     * the context was compiled, or a factory that produced one: a product of a factory is defined by
     * {@code $Factory$...$Definition}, whose bean type is the product and whose declaring type is the factory.
     */
    private boolean wasRegistered(String className) {
        for (BeanDefinitionReference<?> reference : definitionsOf(className)) {
            try {
                if (isRegisteredBean(reference)) {
                    return true;
                }
            } catch (RuntimeException | LinkageError e) {
                return true;
            }
        }
        int lastDot = className.lastIndexOf('.');
        String products = className.substring(0, lastDot + 1) + '$' + className.substring(lastDot + 1) + '$';
        for (BeanDefinitionReference<?> reference : beanContext.getBeanDefinitionReferences()) {
            String name = reference.getBeanDefinitionName();
            if (!name.startsWith(products) || !name.endsWith("$Definition")) {
                continue;
            }
            try {
                if (!isRegisteredBean(reference)) {
                    continue;
                }
                // the name alone matches a nested class too: the declaring type tells the product of this factory
                BeanDefinition<?> definition = reference.load();
                if (definition == null || definition.getDeclaringType().map(Class::getName).filter(className::equals).isPresent()) {
                    return true;
                }
            } catch (RuntimeException | LinkageError e) {
                return true;
            }
        }
        return false;
    }

    /**
     * Whether a bean is one the registries are built from: a payload serializer, a binder, a registry of them, or a
     * publisher client.
     */
    private static boolean isRegisteredBean(BeanType<?> candidate) {
        Class<?> beanType = candidate.getBeanType();
        for (Class<?> registeredType : REGISTERED_TYPES) {
            if (registeredType.isAssignableFrom(beanType)) {
                return true;
            }
        }
        AnnotationMetadata metadata = candidate.getAnnotationMetadata();
        for (Class<? extends Annotation> client : CLIENTS) {
            if (metadata.hasStereotype(client)) {
                return true;
            }
        }
        return false;
    }

    private List<BeanDefinitionReference<?>> definitionsOf(String className) {
        int lastDot = className.lastIndexOf('.');
        // $Name$Definition, the definitions of its proxies and, for an introduced interface, $Name$Intercepted$Definition
        String prefix = className.substring(0, lastDot + 1) + '$' + className.substring(lastDot + 1);
        String definition = prefix + "$Definition";
        List<BeanDefinitionReference<?>> references = new ArrayList<>();
        for (BeanDefinitionReference<?> reference : beanContext.getBeanDefinitionReferences()) {
            String name = reference.getBeanDefinitionName();
            if (name.equals(definition) || name.startsWith(definition + '$') || name.startsWith(prefix + "$Intercepted$Definition")) {
                references.add(reference);
            }
        }
        return references;
    }

    /**
     * Recreates what changed and the subscriber advice, which unsubscribes its topics as it is destroyed. The context
     * creates the advice again at once and gives it the {@code Topic} methods, as at startup, so that it subscribes
     * again on top of the new beans. Nothing is created that was not created already, but for the subscriber beans
     * the advice resolves.
     *
     * @param registries Whether to recreate the payload serializer and binder registries and the publisher advice, and
     * the beans that received them
     * @param listeners The subscriber classes that changed, whose beans are recreated
     * @param reason Why, for the log
     */
    private void restartSubscriptions(boolean registries, List<String> listeners, String reason) {
        if (!(beanContext instanceof WatchableBeanContext context)) {
            return;
        }
        // taken first: recreating one destroys the beans that received it, as the graph records them. The subscriber
        // beans go first, as the advice subscribes again as soon as it is recreated, on the subscriber beans it finds
        // then; it goes last, recreated already, and so skipped, when a registry it received was
        List<Object> beans = new ArrayList<>();
        if (!listeners.isEmpty()) {
            Set<String> names = new HashSet<>(listeners);
            for (BeanRegistration<?> registration : beanContext.getActiveBeanRegistrations(LISTENERS)) {
                if (names.contains(registration.getBeanDefinition().getBeanType().getName())) {
                    add(beans, registration.bean());
                }
            }
        }
        if (registries) {
            for (Class<?> type : REGISTRIES) {
                for (BeanRegistration<?> registration : beanContext.getActiveBeanRegistrations(type)) {
                    add(beans, registration.bean());
                }
            }
        }
        for (BeanRegistration<AbstractMqttSubscriberAdvice> registration : beanContext.getActiveBeanRegistrations(AbstractMqttSubscriberAdvice.class)) {
            add(beans, registration.bean());
        }
        if (beans.isEmpty()) {
            return;
        }
        LOG.debug("Restarting the MQTT subscriptions: {}", reason);
        for (Object bean : beans) {
            // false for a bean destroyed with one recreated before it, and for all of them in a context that does
            // not track bean dependencies: they are kept, and the subscriptions stay until a restart
            context.recreate(bean);
        }
    }

    private static void add(List<Object> beans, Object bean) {
        for (Object taken : beans) {
            if (taken == bean) {
                return;
            }
        }
        beans.add(bean);
    }

    private static List<String> names(BeanDefinitionChange<?> change) {
        List<String> changed = new ArrayList<>();
        for (BeanDefinition<?> definition : change.removed()) {
            changed.add(definition.getBeanType().getName());
        }
        for (BeanDefinition<?> definition : change.added()) {
            changed.add(definition.getBeanType().getName());
        }
        return changed;
    }

    private static boolean ignored(BeanDefinitionChange<?> change) {
        return change.initial() || (change.added().isEmpty() && change.removed().isEmpty());
    }

    /**
     * Restarts the subscriptions when a subscriber definition is registered or removed. The first batch is what the
     * subscriber advice was given at startup.
     */
    private final class ListenerDefinitionsWatcher implements BeanDefinitionWatcher<Object>, Ordered {
        @Override
        public void onChange(BeanDefinitionChange<Object> change) {
            if (!ignored(change)) {
                restartSubscriptions(false, names(change), "subscriber definitions changed");
            }
        }

        @Override
        public int getOrder() {
            return Ordered.LOWEST_PRECEDENCE;
        }
    }

    /**
     * Selects the subscriber beans, whether the stereotype is on the class or only on a method. The class metadata of
     * a bean is read first, which needs nothing loaded. The methods are read only for a bean that requires method
     * processing, as a bean whose methods the subscriber advice receives does; that skips the executable methods of
     * every other bean, and the class that holds them. A reference, which has no methods to read, is kept when it
     * requires method processing.
     */
    private static final class ListenerQualifier implements Qualifier<Object> {
        @Override
        public <B extends BeanType<Object>> Stream<B> reduce(Class<Object> beanType, Stream<B> candidates) {
            return candidates.filter(ListenerQualifier::isListenerBean);
        }

        @Override
        public boolean doesQualify(Class<Object> beanType, BeanType<Object> candidate) {
            return isListenerBean(candidate);
        }

        private static boolean isListenerBean(BeanType<?> candidate) {
            if (hasStereotype(candidate.getAnnotationMetadata())) {
                return true;
            }
            if (!candidate.requiresMethodProcessing()) {
                return false;
            }
            if (!(candidate instanceof BeanDefinition<?> definition)) {
                return true;
            }
            for (ExecutableMethod<?, ?> method : definition.getExecutableMethods()) {
                if (hasStereotype(method.getAnnotationMetadata())) {
                    return true;
                }
            }
            return false;
        }

        private static boolean hasStereotype(AnnotationMetadata metadata) {
            for (Class<? extends Annotation> stereotype : LISTENER_STEREOTYPES) {
                if (metadata.hasStereotype(stereotype)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public String toString() {
            return "subscribers";
        }
    }

    /**
     * Selects the beans the registries are built from: a payload serializer, a binder, a registry of them, or a
     * publisher client.
     */
    private static final class RegisteredQualifier implements Qualifier<Object> {
        @Override
        public <B extends BeanType<Object>> Stream<B> reduce(Class<Object> beanType, Stream<B> candidates) {
            return candidates.filter(DevelopmentMqttReloader::isRegisteredBean);
        }

        @Override
        public boolean doesQualify(Class<Object> beanType, BeanType<Object> candidate) {
            return isRegisteredBean(candidate);
        }

        @Override
        public String toString() {
            return "payload serializers, binders and publishers";
        }
    }

    /**
     * Recreates the registries when a payload serializer, binder, registry or publisher client definition is
     * registered or removed. The first batch is what they were built from.
     */
    private final class RegistryDefinitionsWatcher implements BeanDefinitionWatcher<Object>, Ordered {
        @Override
        public void onChange(BeanDefinitionChange<Object> change) {
            if (!ignored(change)) {
                restartSubscriptions(true, List.of(), names(change) + " registered or removed");
            }
        }

        @Override
        public int getOrder() {
            return Ordered.LOWEST_PRECEDENCE;
        }
    }

    /**
     * Follows a class change applied in place.
     */
    private final class ClassWatcher implements ClassChangeWatcher, Ordered {
        @Override
        public void onChange(ClassChangeEvent change) {
            onClassChange(change);
        }

        @Override
        public int getOrder() {
            return Ordered.LOWEST_PRECEDENCE;
        }
    }

}
