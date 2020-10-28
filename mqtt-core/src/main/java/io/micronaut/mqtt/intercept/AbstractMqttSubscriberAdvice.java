package io.micronaut.mqtt.intercept;

import io.micronaut.context.BeanContext;
import io.micronaut.context.processor.ExecutableMethodProcessor;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.bind.ArgumentBinder;
import io.micronaut.core.bind.BoundExecutable;
import io.micronaut.core.bind.exceptions.UnsatisfiedArgumentException;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.ConversionError;
import io.micronaut.core.convert.exceptions.ConversionErrorException;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.Executable;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.messaging.exceptions.MessageListenerException;
import io.micronaut.mqtt.annotation.MqttSubscriber;
import io.micronaut.mqtt.annotation.Qos;
import io.micronaut.mqtt.annotation.Topic;
import io.micronaut.mqtt.bind.MqttBinder;
import io.micronaut.mqtt.bind.MqttBinderRegistry;
import io.micronaut.mqtt.bind.MqttBindingContext;
import io.micronaut.mqtt.exception.MqttSubscriberException;
import io.micronaut.mqtt.exception.MqttSubscriberExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Qualifier;
import java.util.*;
import java.util.function.Consumer;

public abstract class AbstractMqttSubscriberAdvice<M> implements ExecutableMethodProcessor<MqttSubscriber>, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractMqttSubscriberAdvice.class);

    private final BeanContext beanContext;
    private final MqttBinderRegistry binderRegistry;
    private Set<String> topics = new HashSet<>();

    public AbstractMqttSubscriberAdvice(BeanContext beanContext,
                                        MqttBinderRegistry binderRegistry) {
        this.beanContext = beanContext;
        this.binderRegistry = binderRegistry;
    }

    @Override
    public void process(BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
        AnnotationValue<Topic> topicAnn = method.getAnnotation(Topic.class);
        if (topicAnn != null) {
            io.micronaut.context.Qualifier<Object> qualifer = beanDefinition
                    .getAnnotationTypeByStereotype(Qualifier.class)
                    .map(type -> Qualifiers.byAnnotation(beanDefinition, type))
                    .orElse(null);

            Class<Object> beanType = (Class<Object>) beanDefinition.getBeanType();

            Object bean = beanContext.findBean(beanType, qualifer).orElseThrow(() -> new MqttSubscriberException("Could not find the bean to execute the method " + method));

            try {
                Argument<?>[] arguments = method.getArguments();
                MqttBinder<MqttBindingContext<?>, ?>[] binders = new MqttBinder[arguments.length];
                for (int i = 0; i < arguments.length; i++) {
                    binders[i] = (MqttBinder<MqttBindingContext<?>, ?>) binderRegistry.findArgumentBinder(arguments[i]);
                }

                String topic = topicAnn.stringValue().orElseThrow(() -> new MqttSubscriberException("No topic set to subscribe to"));
                topics.add(topic);
                int qos = method.intValue(Qos.class).orElse(Qos.DEFAULT_VALUE);
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Subscribing to {} with Qos {}", topic, qos);
                }
                subscribe(topic, qos, (context) -> {
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Received the following message from {}", context.getTopic());
                        LOG.trace("Qos = {}, MessageId = {}, Payload = {}", context.getQos(), context.getId(), new String(context.getPayload()));
                    }
                    try {
                        Object result = bind((Executable<Object, Object>) method, arguments, binders, context).invoke(bean);
                    } catch (Exception e) {
                        handleException(e, bean);
                    }
                });
            } catch (Exception e) {
                handleException(e, bean);
            }
        }
    }

    public abstract void subscribe(String topic, int qos, Consumer<MqttBindingContext<M>> callback);

    public abstract void unsubscribe(Set<String> topics);

    @Override
    public void close() throws Exception {
        unsubscribe(topics);
    }

    private void handleException(Exception e, Object bean) {
        if (bean instanceof MqttSubscriberExceptionHandler) {
            ((MqttSubscriberExceptionHandler) bean).handle(e);
        } else {
            LOG.error("Failed to subscribe", e);
        }
    }

    private BoundExecutable bind(
            Executable<Object, Object> target,
            Argument<?>[] arguments,
            MqttBinder<MqttBindingContext<?>, ?>[] binders,
            MqttBindingContext context) {

        Object[] boundArguments = new Object[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            Argument<?> argument = arguments[i];
            ArgumentConversionContext<?> conversionContext = ConversionContext.of(argument);
            Optional<?> result = binders[i].bindFrom(
                    context,
                    conversionContext
            );
            if (!result.isPresent()) {
                if (argument.getAnnotationMetadata().hasAnnotation(Nullable.class)) {
                    boundArguments[i] = null;
                } else {
                    final Optional<ConversionError> lastError = conversionContext.getLastError();
                    if (lastError.isPresent()) {
                        throw new ConversionErrorException(argument, lastError.get());
                    } else {
                        throw new UnsatisfiedArgumentException(argument);
                    }
                }
            } else {
                boundArguments[i] = result.get();
            }
        }

        return new BoundExecutable<Object, Object>() {
            @Override
            public Executable<Object, Object> getTarget() {
                return target;
            }

            @Override
            public Object invoke(Object instance) {
                return target.invoke(instance, getBoundArguments());
            }

            @Override
            public Object[] getBoundArguments() {
                return boundArguments;
            }
        };
    }
}
