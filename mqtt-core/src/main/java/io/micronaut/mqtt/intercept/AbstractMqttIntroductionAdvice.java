/*
 * Copyright 2017-2020 original authors
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

import io.micronaut.aop.InterceptedMethod;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.caffeine.cache.Cache;
import io.micronaut.caffeine.cache.Caffeine;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.mqtt.annotation.Qos;
import io.micronaut.mqtt.annotation.Retained;
import io.micronaut.mqtt.annotation.Topic;
import io.micronaut.mqtt.bind.MqttBinder;
import io.micronaut.mqtt.bind.MqttBinderRegistry;
import io.micronaut.mqtt.bind.MqttBindingContext;
import io.micronaut.mqtt.exception.MqttClientException;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 * Intercepts calls to MQTT clients to publish messages.
 *
 * @param <L> The listener type
 * @param <M> The message type
 * @author James Kleeh
 * @since 1.0.0
 */
public abstract class AbstractMqttIntroductionAdvice<L, M> implements MethodInterceptor<Object, Object> {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractMqttIntroductionAdvice.class);

    private final Cache<ExecutableMethod<?, ?>, MqttPublisherState> publisherCache = Caffeine.newBuilder().build();
    private final MqttBinderRegistry binderRegistry;

    public AbstractMqttIntroductionAdvice(MqttBinderRegistry binderRegistry) {
        this.binderRegistry = binderRegistry;
    }

    @Override
    public Object intercept(MethodInvocationContext<Object, Object> context) {
        if (context.hasAnnotation(getRequiredAnnotation())) {

            InterceptedMethod interceptedMethod = InterceptedMethod.of(context);

            try {
                MqttPublisherState publisherState = getPublisherState(context);

                switch (interceptedMethod.resultType()) {
                    case PUBLISHER:
                        return interceptedMethod.handleResult(Flowable.create(emitter -> {
                            publish(publisherState, context, createListener(emitter::onComplete, emitter::onError));
                        }, BackpressureStrategy.ERROR));
                    case COMPLETION_STAGE:
                        CompletableFuture<Void> future = new CompletableFuture<>();
                        publish(publisherState, context, createListener(() -> future.complete(null), future::completeExceptionally));
                        return interceptedMethod.handleResult(future);
                    case SYNCHRONOUS:
                        CountDownLatch countDownLatch = new CountDownLatch(1);
                        AtomicReference<Throwable> error = new AtomicReference<>();
                        Object token = publish(publisherState, context, createListener(countDownLatch::countDown, (t) -> {
                            error.set(t);
                            countDownLatch.countDown();
                        }));
                        try {
                            countDownLatch.await();
                        } catch (InterruptedException e) {
                            throw new MqttClientException("Publish thread interrupted waiting for a response", e);
                        }
                        if (error.get() == null) {
                            return interceptedMethod.handleResult(token);
                        } else {
                            throw new MqttClientException("Failed to publish the message", error.get());
                        }
                    default:
                        return interceptedMethod.unsupported();
                }
            } catch (Exception e) {
                return interceptedMethod.handleException(e);
            }
        }

        return context.proceed();
    }

    /**
     * @param context The method context
     * @return The binding context to bind data used in publishing
     */
    public abstract MqttBindingContext<M> createBindingContext(MethodInvocationContext<Object, Object> context);

    /**
     * Publishes the message.
     *
     * @param topic The topic to publish the message to
     * @param message The message to publish
     * @param listener The publish action listener
     * @return The result of the publish method
     */
    public abstract Object publish(String topic, M message, L listener);

    /**
     * Create a listener.
     *
     * @param onSuccess The code to call when a message is sent successfully
     * @param onError The consumer to call in the case of an exception
     * @return A publish action listener
     */
    public abstract L createListener(Runnable onSuccess, Consumer<Throwable> onError);

    /**
     * @return The implement specific client annotation class
     */
    public abstract Class<? extends Annotation> getRequiredAnnotation();

    private MqttPublisherState getPublisherState(MethodInvocationContext<Object, Object> context) {
        return publisherCache.get(context.getExecutableMethod(), (method) -> {
            MqttPublisherState state = new MqttPublisherState();

            method.findAnnotation(Topic.class)
                    .ifPresent(topicAnn -> {
                        topicAnn.stringValue().ifPresent(state::setTopic);
                        topicAnn.intValue("qos").ifPresent(state::setQos);
                    });
            method.findAnnotation(Qos.class)
                    .ifPresent((av) -> av.intValue().ifPresent(state::setQos));
            method.findAnnotation(Retained.class)
                    .flatMap(AnnotationValue::booleanValue)
                    .ifPresent(state::setRetained);

            for (Argument<?> argument: context.getArguments()) {
                state.setBinder(argument, (MqttBinder<Object, Object>) binderRegistry.findArgumentBinder(argument));
            }
            return state;
        });
    }

    private Object publish(MqttPublisherState state, MethodInvocationContext<Object, Object> context, L listener) {
        MqttBindingContext<M> bindingContext = createBindingContext(context);
        Integer qos = state.getQos();
        if (qos != null) {
            bindingContext.setQos(qos);
        }
        Boolean retained = state.getRetained();
        if (retained != null) {
            bindingContext.setRetained(retained);
        }
        String topic = state.getTopic();
        if (topic != null) {
            bindingContext.setTopic(topic);
        }
        state.bind(bindingContext, context);
        topic = bindingContext.getTopic();
        if (topic == null) {
            throw new MqttClientException("The topic was not found in any @Topic annotation or method argument");
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("Publishing the following message to {}", bindingContext.getTopic());
            LOG.trace("Qos = {}, Retained = {}, Payload = {}", bindingContext.getQos(), bindingContext.isRetained(), new String(bindingContext.getPayload()));
        }
        return publish(bindingContext.getTopic(), bindingContext.getNativeMessage(), listener);
    }
}
