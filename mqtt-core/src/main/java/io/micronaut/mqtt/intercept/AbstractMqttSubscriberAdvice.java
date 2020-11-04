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

import io.micronaut.context.BeanContext;
import io.micronaut.context.processor.ExecutableMethodProcessor;
import io.micronaut.core.annotation.AnnotationValue;
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
import io.micronaut.mqtt.annotation.MqttSubscriber;
import io.micronaut.mqtt.annotation.Topic;
import io.micronaut.mqtt.bind.MqttBinder;
import io.micronaut.mqtt.bind.MqttBinderRegistry;
import io.micronaut.mqtt.bind.MqttBindingContext;
import io.micronaut.mqtt.exception.MqttSubscriberException;
import io.micronaut.mqtt.exception.MqttSubscriberExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

/**
 * An {@link ExecutableMethodProcessor} that will process all beans annotated
 * with {@link MqttSubscriber} and create and subscribe the relevant methods
 * as consumers to MQTT topics.
 *
 * @param <M> The specific message type
 * @author James Kleeh
 * @since 1.0.0
 */
public abstract class AbstractMqttSubscriberAdvice<M> implements ExecutableMethodProcessor<MqttSubscriber>, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractMqttSubscriberAdvice.class);

    private final BeanContext beanContext;
    private final MqttBinderRegistry binderRegistry;
    private final MqttSubscriberExceptionHandler exceptionHandler;
    private final Set<String> topics = new HashSet<>();

    public AbstractMqttSubscriberAdvice(BeanContext beanContext,
                                        MqttBinderRegistry binderRegistry,
                                        MqttSubscriberExceptionHandler exceptionHandler) {
        this.beanContext = beanContext;
        this.binderRegistry = binderRegistry;
        this.exceptionHandler = exceptionHandler;
    }

    @Override
    public void process(BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
        List<AnnotationValue<Topic>> topicAnnotations = method.getAnnotationValuesByType(Topic.class);
        if (!topicAnnotations.isEmpty()) {

            Object bean = beanContext.getBean(beanDefinition);

            try {
                Argument[] arguments = method.getArguments();
                MqttBinder<MqttBindingContext<?>, Object>[] binders = new MqttBinder[arguments.length];
                for (int i = 0; i < arguments.length; i++) {
                    binders[i] = (MqttBinder<MqttBindingContext<?>, Object>) binderRegistry.findArgumentBinder(arguments[i]);
                }

                String[] topicValues = new String[topicAnnotations.size()];
                int[] qosValues = new int[topicAnnotations.size()];

                for (int i = 0; i < topicAnnotations.size(); i++) {
                    AnnotationValue<Topic> topicAnn = topicAnnotations.get(i);
                    topicValues[i] = topicAnn.getRequiredValue(String.class); //the value is required
                    qosValues[i] = topicAnn.getRequiredValue("qos", int.class);
                }

                topics.addAll(Arrays.asList(topicValues));
                if (LOG.isTraceEnabled()) {
                    for (int i = 0; i < topicValues.length; i++) {
                        LOG.trace("Subscribing to {} with Qos {}", topicValues[i], qosValues[i]);
                    }
                }
                subscribe(topicValues, qosValues, (context) -> {
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Received the following message from {}", context.getTopic());
                        LOG.trace("Qos = {}, MessageId = {}, Payload = {}", context.getQos(), context.getId(), new String(context.getPayload()));
                    }
                    try {
                        Object result = bind((Executable<Object, Object>) method, arguments, binders, context).invoke(bean);
                    } catch (Exception e) {
                        handleException(e, bean, method);
                    }
                });
            } catch (Exception e) {
                handleException(e, bean, method);
            }
        }
    }

    public abstract void subscribe(String[] topics, int[] qos, Consumer<MqttBindingContext<M>> callback);

    public abstract void unsubscribe(Set<String> topics);

    @Override
    public void close() throws Exception {
        unsubscribe(topics);
    }

    private void handleException(Exception e, Object bean, ExecutableMethod<?, ?> method) {
        if (!(e instanceof MqttSubscriberException)) {
            e = new MqttSubscriberException(String.format("Subscriber [%s] encountered an error", method), e);
        }
        if (bean instanceof MqttSubscriberExceptionHandler) {
            ((MqttSubscriberExceptionHandler) bean).handle((MqttSubscriberException) e);
        } else {
            exceptionHandler.handle((MqttSubscriberException) e);
        }
    }

    private BoundExecutable bind(
            Executable<Object, Object> target,
            Argument<?>[] arguments,
            MqttBinder<MqttBindingContext<?>, Object>[] binders,
            MqttBindingContext context) {

        Object[] boundArguments = new Object[arguments.length];
        for (int i = 0; i < arguments.length; i++) {
            Argument argument = arguments[i];
            ArgumentConversionContext<Object> conversionContext = ConversionContext.of(argument);
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
