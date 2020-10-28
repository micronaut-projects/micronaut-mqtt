package io.micronaut.mqtt.intercept;

import io.micronaut.aop.InterceptedMethod;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.caffeine.cache.Cache;
import io.micronaut.caffeine.cache.Caffeine;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.MutableArgumentValue;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.mqtt.annotation.Qos;
import io.micronaut.mqtt.annotation.Retained;
import io.micronaut.mqtt.annotation.Topic;
import io.micronaut.mqtt.bind.MqttBinder;
import io.micronaut.mqtt.bind.MqttBinderRegistry;
import io.micronaut.mqtt.bind.MqttBindingContext;
import io.micronaut.mqtt.exception.MqttClientException;
import io.micronaut.mqtt.serdes.MqttPayloadSerDesRegistry;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

/**
 *
 * @param <L> The listener type
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

    protected MqttPublisherState getPublisherState(MethodInvocationContext<Object, Object> context) {
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

            for (Argument<?> argument: context.getArguments()) {
                state.setBinder(argument, (MqttBinder<Object, Object>) binderRegistry.findArgumentBinder(argument));
            }
            return state;
        });
    }

    public abstract MqttBindingContext<M> createBindingContext(MethodInvocationContext<Object, Object> context);

    public abstract Object publish(String topic, M message, L listener);

    public abstract L createListener(Runnable onSuccess, Consumer<Throwable> onError);

    public abstract Class<? extends Annotation> getRequiredAnnotation();

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
