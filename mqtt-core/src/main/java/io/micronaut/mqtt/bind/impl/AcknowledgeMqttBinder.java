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
import io.micronaut.messaging.Acknowledgement;
import io.micronaut.messaging.exceptions.MessageAcknowledgementException;
import io.micronaut.mqtt.bind.MqttBindingContext;
import io.micronaut.mqtt.bind.TypedMqttBinder;
import jakarta.inject.Singleton;

import java.util.Optional;

/**
 * A typed binder for {@link Acknowledgement} that allows subscribers to manually
 * acknowledge messages. An {@link Acknowledgement} must be an argument to subscriber methods
 * and used when manual acknowledgements is set to true in the configuration.
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@Singleton
public class AcknowledgeMqttBinder implements TypedMqttBinder<MqttBindingContext<?>, Acknowledgement> {

    @Override
    public Argument<Acknowledgement> getArgumentType() {
        return Argument.of(Acknowledgement.class);
    }

    @Override
    public void bindTo(MqttBindingContext<?> context, Acknowledgement value, Argument<Acknowledgement> argument) {
    }

    @Override
    public Optional<Acknowledgement> bindFrom(MqttBindingContext<?> context, ArgumentConversionContext<Acknowledgement> conversionContext) {
        return Optional.of(new Acknowledgement() {
            @Override
            public void ack() throws MessageAcknowledgementException {
                context.acknowlege();
            }

            @Override
            public void nack() throws MessageAcknowledgementException {
                throw new UnsupportedOperationException("Mqtt only supports acknowledging messages");
            }
        });
    }
}
