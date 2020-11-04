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
import io.micronaut.mqtt.annotation.Id;
import io.micronaut.mqtt.bind.AnnotatedMqttBinder;
import io.micronaut.mqtt.bind.MqttBindingContext;
import io.micronaut.mqtt.exception.MqttClientException;

import javax.inject.Singleton;
import java.util.Optional;

/**
 * An annotated argument binder for {@link Id}.
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@Singleton
public class IdMqttBinder implements AnnotatedMqttBinder<MqttBindingContext<?>, Id> {

    private final ConversionService<?> conversionService;

    public IdMqttBinder(ConversionService<?> conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public Class<Id> getAnnotationType() {
        return Id.class;
    }

    @Override
    public void bindTo(MqttBindingContext<?> context, Object value, Argument<Object> argument) {
        throw new MqttClientException("The message ID cannot be set when publishing messages");
    }

    @Override
    public Optional<Object> bindFrom(MqttBindingContext<?> context, ArgumentConversionContext<Object> conversionContext) {
        return conversionService.convert(context.getId(), conversionContext);
    }
}
