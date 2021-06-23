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
package io.micronaut.mqtt.bind.impl;

import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.mqtt.annotation.Retained;
import io.micronaut.mqtt.bind.AnnotatedMqttBinder;
import io.micronaut.mqtt.bind.MqttBindingContext;
import jakarta.inject.Singleton;

import java.util.Optional;

/**
 * An annotated argument binder for {@link Retained}.
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@Singleton
public class RetainedMqttBinder implements AnnotatedMqttBinder<MqttBindingContext<?>, Retained> {

    private final ConversionService<?> conversionService;

    public RetainedMqttBinder(ConversionService<?> conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public Class<Retained> getAnnotationType() {
        return Retained.class;
    }

    @Override
    public void bindTo(MqttBindingContext<?> message, Object value, Argument<Object> argument) {
        conversionService.convert(value, Argument.BOOLEAN)
                .ifPresent(message::setRetained);
    }

    @Override
    public Optional<Object> bindFrom(MqttBindingContext<?> message, ArgumentConversionContext<Object> conversionContext) {
        return conversionService.convert(message.isRetained(), conversionContext);
    }
}
