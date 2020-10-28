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
import io.micronaut.mqtt.annotation.Topic;
import io.micronaut.mqtt.bind.AnnotatedMqttBinder;
import io.micronaut.mqtt.bind.MqttBindingContext;

import javax.inject.Singleton;
import java.util.Optional;

@Singleton
public class TopicMqttBinder implements AnnotatedMqttBinder<MqttBindingContext<?>, Topic> {

    private final ConversionService<?> conversionService;

    public TopicMqttBinder(ConversionService<?> conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public Class<Topic> getAnnotationType() {
        return Topic.class;
    }

    @Override
    public void bindTo(MqttBindingContext<?> context, Object value, Argument<?> argument) {
        conversionService.convert(value, Argument.STRING)
                .ifPresent(context::setTopic);
    }

    @Override
    public Optional<?> bindFrom(MqttBindingContext<?> context, ArgumentConversionContext<?> conversionContext) {
        return conversionService.convert(context.getTopic(), conversionContext);
    }
}
