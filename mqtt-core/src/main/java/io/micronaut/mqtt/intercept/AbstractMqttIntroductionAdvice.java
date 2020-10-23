package io.micronaut.mqtt.intercept;

import io.micronaut.aop.InterceptedMethod;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.caffeine.cache.Cache;
import io.micronaut.caffeine.cache.Caffeine;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.bind.annotation.Bindable;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.MutableArgumentValue;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.messaging.annotation.Body;
import io.micronaut.mqtt.annotation.Qos;
import io.micronaut.mqtt.annotation.Retained;
import io.micronaut.mqtt.annotation.Topic;
import io.micronaut.mqtt.bind.MqttBinder;
import io.micronaut.mqtt.bind.MqttBinderRegistry;
import io.micronaut.mqtt.bind.MqttMessage;
import io.micronaut.mqtt.exception.MqttClientException;
import io.micronaut.mqtt.serdes.MqttPayloadSerDesRegistry;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 *
 * @param <L> The listener type
 */
public abstract class AbstractMqttIntroductionAdvice<L, M> implements MethodInterceptor<Object, Object> {

    private final Cache<ExecutableMethod<?, ?>, MqttPublisherState> publisherCache = Caffeine.newBuilder().build();
    private final ConversionService<?> conversionService;
    private final MqttPayloadSerDesRegistry serDesRegistry;
    private final MqttBinderRegistry binderRegistry;

    public AbstractMqttIntroductionAdvice(ConversionService<?> conversionService,
                                          MqttPayloadSerDesRegistry serDesRegistry,
                                          MqttBinderRegistry binderRegistry) {
        this.conversionService = conversionService;
        this.serDesRegistry = serDesRegistry;
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

    public MqttPublisherState getPublisherState(MethodInvocationContext<Object, Object> context) {
        return publisherCache.get(context.getExecutableMethod(), (method) -> {
            MqttPublisherState state = new MqttPublisherState();

            method.findAnnotation(Topic.class)
                    .flatMap(AnnotationValue::stringValue)
                    .ifPresent(state::setTopic);
            method.findAnnotation(Qos.class)
                    .ifPresent((av) -> av.intValue().ifPresent(state::setQos));
            method.findAnnotation(Retained.class)
                    .flatMap(AnnotationValue::booleanValue)
                    .ifPresent(state::setRetained);

            for (Map.Entry<String, MutableArgumentValue<?>> entry: context.getParameters().entrySet()) {
                MutableArgumentValue<?> argument = entry.getValue();
                binderRegistry.findArgumentBinder(argument)
                        .ifPresent(binder -> state.setBinder(argument, (MqttBinder<Object, Object>) binder));
            }
            return state;
        });
    }

    public abstract MqttMessage<M> createMessage();

    public abstract Object publish(String topic, M message, L listener);

    public abstract L createListener(Runnable onSuccess, Consumer<Throwable> onError);

    public abstract Class<? extends Annotation> getRequiredAnnotation();

    private Object publish(MqttPublisherState state, MethodInvocationContext<Object, Object> context, L listener) {
        MqttMessage<M> message = createMessage();
        Integer qos = state.getQos();
        if (qos != null) {
            message.setQos(qos);
        }
        Boolean retained = state.getRetained();
        if (retained != null) {
            message.setRetained(retained);
        }
        String topic = state.getTopic();
        if (topic != null) {
            message.setTopic(topic);
        }
        state.bind(message, context);
        topic = message.getTopic();
        if (topic == null) {
            throw new MqttClientException("The topic was not found in any @Topic annotation or method argument");
        }
        return publish(message.getTopic(), message.getNativeMessage(), listener);
    }
}
