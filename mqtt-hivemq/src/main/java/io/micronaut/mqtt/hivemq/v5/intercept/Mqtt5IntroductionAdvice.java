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
package io.micronaut.mqtt.hivemq.v5.intercept;

import com.hivemq.client.internal.mqtt.message.publish.MqttPublish;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserPropertiesBuilder;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PayloadFormatIndicator;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishResult;
import io.micronaut.aop.InterceptorBean;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.beans.BeanProperty;
import io.micronaut.core.util.StringUtils;
import io.micronaut.mqtt.annotation.v5.MqttProperty;
import io.micronaut.mqtt.annotation.v5.MqttPublisher;
import io.micronaut.mqtt.bind.MqttBinderRegistry;
import io.micronaut.mqtt.bind.MqttBindingContext;
import io.micronaut.mqtt.exception.MqttClientException;
import io.micronaut.mqtt.hivemq.bind.MqttMessage;
import io.micronaut.mqtt.hivemq.bind.UserProperty;
import io.micronaut.mqtt.hivemq.v5.bind.MqttV5BindingContext;
import io.micronaut.mqtt.intercept.AbstractMqttIntroductionAdvice;
import jakarta.inject.Singleton;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * The MQTT v5 implementation of {@link AbstractMqttIntroductionAdvice}.
 *
 * @author Sven Kobow
 * @since 3.0.0
 */
@Singleton
@InterceptorBean(MqttPublisher.class)
@Requires(property = "mqtt.client.mqtt-version", value = "5", defaultValue = "5")
public class Mqtt5IntroductionAdvice extends AbstractMqttIntroductionAdvice<BiConsumer<Mqtt5PublishResult, Throwable>, MqttMessage> {

    private final Mqtt5AsyncClient mqtt5AsyncClient;

    public Mqtt5IntroductionAdvice(Mqtt5AsyncClient mqttAsyncClient,
                                   MqttBinderRegistry binderRegistry) {
        super(binderRegistry);
        this.mqtt5AsyncClient = mqttAsyncClient;
    }

    @Override
    public Object publish(String topic, MqttMessage message, BiConsumer<Mqtt5PublishResult, Throwable> listener) {

        final Mqtt5UserPropertiesBuilder userPropertiesBuilder = Mqtt5UserProperties.builder();
        message.getUserProperties().forEach(prop -> userPropertiesBuilder.add(prop.getKey(), prop.getValue()));

        final var publishBuilder = Mqtt5Publish.builder()
            .topic(topic)
            .payload(message.getPayload())
            .qos(Objects.requireNonNull(MqttQos.fromCode(message.getQos())))
            .retain(message.isRetained())
            .contentType(message.getContentType())
            .payloadFormatIndicator(Mqtt5PayloadFormatIndicator.fromCode(message.getPayloadFormatIndicator()))
            .userProperties(userPropertiesBuilder.build())
            .correlationData(message.getCorrelationData())
            .responseTopic(message.getResponseTopic());

        if (message.getMessageExpiryInterval() != MqttPublish.NO_MESSAGE_EXPIRY) {
            publishBuilder.messageExpiryInterval(message.getMessageExpiryInterval());
        }

        mqtt5AsyncClient.publish(publishBuilder.build())
            .exceptionally(throwable -> {
                throw new MqttClientException("Failed to publish the message", throwable);
            })
            .whenComplete(listener);

        return null;
    }

    @Override
    public MqttBindingContext<MqttMessage> createBindingContext(MethodInvocationContext<Object, Object> context) {
        final MqttMessage message = new MqttMessage();
        final List<AnnotationValue<MqttProperty>> propertyAnnotations = context.getAnnotationValuesByType(MqttProperty.class);
        final List<UserProperty> properties = new ArrayList<>();
        propertyAnnotations.forEach(prop -> {
            final String name = prop.get("name", String.class).orElse(null);
            final String value = prop.getValue(String.class).orElse(null);
            final BeanIntrospection<MqttMessage> introspection = BeanIntrospection.getIntrospection(MqttMessage.class);
            if (StringUtils.isNotEmpty(name) && StringUtils.isNotEmpty(value)) {
                final Optional<BeanProperty<MqttMessage, Object>> property = introspection.getProperty(name);
                if (property.isPresent()) {
                    property.get().convertAndSet(message, value);
                } else {
                    properties.add(new UserProperty(name, value));
                }
            }
        });
        message.setUserProperties(properties);
        return new MqttV5BindingContext(mqtt5AsyncClient, message);
    }

    @Override
    public BiConsumer<Mqtt5PublishResult, Throwable> createListener(Runnable onSuccess, Consumer<Throwable> onError) {
        return (mqtt5PublishResult, throwable) -> {
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
