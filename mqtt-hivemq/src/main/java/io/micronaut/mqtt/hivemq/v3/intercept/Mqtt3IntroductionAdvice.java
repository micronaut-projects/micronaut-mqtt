/*
 * Copyright 2017-2023 original authors
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
package io.micronaut.mqtt.hivemq.v3.intercept;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import io.micronaut.aop.InterceptorBean;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.mqtt.annotation.v3.MqttPublisher;
import io.micronaut.mqtt.bind.MqttBinderRegistry;
import io.micronaut.mqtt.bind.MqttBindingContext;
import io.micronaut.mqtt.exception.MqttClientException;
import io.micronaut.mqtt.hivemq.bind.MqttMessage;
import io.micronaut.mqtt.hivemq.v3.bind.MqttV3BindingContext;
import io.micronaut.mqtt.intercept.AbstractMqttIntroductionAdvice;
import jakarta.inject.Singleton;

import java.lang.annotation.Annotation;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * The MQTT v3 implementation of {@link AbstractMqttIntroductionAdvice}.
 *
 * @author Sven Kobow
 * @since 3.0.0
 */
@Singleton
@InterceptorBean(MqttPublisher.class)
@Requires(property = "mqtt.client.mqtt-version", value = "3")
public class Mqtt3IntroductionAdvice extends AbstractMqttIntroductionAdvice<BiConsumer<Mqtt3Publish, Throwable>, MqttMessage> {
    private final Mqtt3AsyncClient mqtt3AsyncClient;

    public Mqtt3IntroductionAdvice(final Mqtt3AsyncClient mqtt3AsyncClient,
                                   final MqttBinderRegistry binderRegistry) {
        super(binderRegistry);
        this.mqtt3AsyncClient = mqtt3AsyncClient;
    }

    @Override
    public MqttBindingContext<MqttMessage> createBindingContext(MethodInvocationContext<Object, Object> context) {
        return new MqttV3BindingContext(mqtt3AsyncClient, new MqttMessage());
    }

    @Override
    public Object publish(String topic, MqttMessage message, BiConsumer<Mqtt3Publish, Throwable> listener) {

        final CompletableFuture<Mqtt3Publish> publishFuture = mqtt3AsyncClient.publishWith()
            .topic(topic)
            .payload(message.getPayload())
            .qos(Objects.requireNonNull(MqttQos.fromCode(message.getQos())))
            .retain(message.isRetained())
            .send();

        publishFuture
            .exceptionally(throwable -> {
                throw new MqttClientException("Failed to publish the message", throwable);
            })
            .whenComplete(listener);

        return null;
    }

    @Override
    public BiConsumer<Mqtt3Publish, Throwable> createListener(Runnable onSuccess, Consumer<Throwable> onError) {
        return (mqtt3Publish, throwable) -> {
            if (throwable != null) {
                onError.accept(throwable);
            } else {
                onSuccess.run();
            }
        };
    }

    @Override
    public Class<? extends Annotation> getRequiredAnnotation() {
        return MqttPublisher.class;
    }
}
