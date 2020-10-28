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

import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.beans.BeanProperty;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.type.Argument;
import io.micronaut.mqtt.bind.FallbackMqttBinder;
import io.micronaut.mqtt.bind.MqttBindingContext;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Singleton
public class DefaultMqttBinder implements FallbackMqttBinder<MqttBindingContext<?>> {

    private final Map<String, BeanProperty<MqttBindingContext, ?>> properties = new HashMap<>();
    private final PayloadMqttBinder payloadMqttBinder;

    public DefaultMqttBinder(PayloadMqttBinder payloadMqttBinder) {
        this.payloadMqttBinder = payloadMqttBinder;
        BeanIntrospection<MqttBindingContext> introspection = BeanIntrospection.getIntrospection(MqttBindingContext.class);
        this.properties.put("qos", introspection.getRequiredProperty("qos", int.class));
        this.properties.put("retained", introspection.getRequiredProperty("retained", boolean.class));
        this.properties.put("topic", introspection.getRequiredProperty("topic", String.class));
        this.properties.put("id", introspection.getRequiredProperty("id", int.class));
    }

    @Override
    public void bindTo(MqttBindingContext<?> context, Object value, Argument<?> argument) {
        BeanProperty<MqttBindingContext, ?> property = this.properties.get(argument.getName());
        if (property != null) {
            if (!property.isReadOnly()) {
                property.convertAndSet(context, value);
            }
        } else {
            payloadMqttBinder.bindTo(context, value, argument);
        }
    }

    @Override
    public Optional<?> bindFrom(MqttBindingContext<?> context, ArgumentConversionContext<?> conversionContext) {
        BeanProperty<MqttBindingContext, ?> property = this.properties.get(conversionContext.getArgument().getName());
        if (property != null) {
            return property.get(context, conversionContext);
        } else {
            return payloadMqttBinder.bindFrom(context, conversionContext);
        }
    }
}
