/*
 * Copyright 2017-2022 original authors
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
package io.micronaut.mqtt.bind.impl;

import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.type.Argument;
import io.micronaut.messaging.annotation.MessageBody;
import io.micronaut.mqtt.bind.AnnotatedMqttBinder;
import io.micronaut.mqtt.bind.MqttBindingContext;
import io.micronaut.mqtt.serdes.MqttPayloadSerDes;
import io.micronaut.mqtt.serdes.MqttPayloadSerDesRegistry;
import jakarta.inject.Singleton;

import java.util.Optional;

/**
 * A binder responsible for binding to the payload of the message.
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@Singleton
public class PayloadMqttBinder implements AnnotatedMqttBinder<MqttBindingContext<?>, MessageBody> {

    private final MqttPayloadSerDesRegistry serDesRegistry;

    public PayloadMqttBinder(MqttPayloadSerDesRegistry serDesRegistry) {
        this.serDesRegistry = serDesRegistry;
    }

    @Override
    public Class<MessageBody> getAnnotationType() {
        return MessageBody.class;
    }

    @Override
    public void bindTo(MqttBindingContext<?> context, Object value, Argument<Object> argument) {
        serDesRegistry.findSerdes(argument)
                .map(serDes -> ((MqttPayloadSerDes<Object>) serDes).serialize(value))
                .ifPresent(context::setPayload);
    }

    @Override
    public Optional<Object> bindFrom(MqttBindingContext<?> context, ArgumentConversionContext<Object> conversionContext) {
        return serDesRegistry.findSerdes(conversionContext.getArgument())
                .map(serDes -> serDes.deserialize(context.getPayload(), conversionContext.getArgument()));
    }
}
