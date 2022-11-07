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
package io.micronaut.mqtt.v5.bind;

import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.beans.BeanProperty;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.mqtt.bind.AnnotatedMqttBinder;
import io.micronaut.mqtt.v5.annotation.MqttProperty;
import jakarta.inject.Singleton;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.eclipse.paho.mqttv5.common.packet.UserProperty;

import java.util.List;
import java.util.Optional;

/**
 * Binds arguments to and from {@link MqttProperties}.
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@Introspected(classes = MqttProperties.class)
@Singleton
public class MqttPropertyBinder implements AnnotatedMqttBinder<MqttV5BindingContext, MqttProperty> {

    private final ConversionService conversionService;
    private final BeanIntrospection<MqttProperties> introspection;

    /**
     * @param conversionService The conversion service
     */
    public MqttPropertyBinder(ConversionService conversionService) {
        this.conversionService = conversionService;
        this.introspection = BeanIntrospection.getIntrospection(MqttProperties.class);
    }

    @Override
    public Class<MqttProperty> getAnnotationType() {
        return MqttProperty.class;
    }

    @Override
    public void bindTo(MqttV5BindingContext context, Object value, Argument<Object> argument) {
        String name = getParameterName(argument);
        Optional<BeanProperty<MqttProperties, Object>> property = introspection.getProperty(name);
        if (property.isPresent()) {
            property.get().convertAndSet(context.getProperties(), value);
        } else {
            List<UserProperty> userProperties = context.getProperties().getUserProperties();
            userProperties.removeIf(up -> up.getKey().equals(name));
            conversionService.convert(value, Argument.STRING)
                    .map(val -> new UserProperty(name, val))
                    .ifPresent(userProperties::add);
        }
    }

    @Override
    public Optional<Object> bindFrom(MqttV5BindingContext context, ArgumentConversionContext<Object> conversionContext) {
        String name = getParameterName(conversionContext.getArgument());
        Optional<BeanProperty<MqttProperties, Object>> property = introspection.getProperty(name);
        if (property.isPresent()) {
            return property.get().get(context.getProperties(), conversionContext);
        } else {
            List<UserProperty> userProperties = context.getProperties().getUserProperties();
            for (UserProperty up: userProperties) {
                if (up.getKey().equals(name)) {
                    return conversionService.convert(up.getValue(), conversionContext);
                }
            }
        }
        return Optional.empty();
    }

    private String getParameterName(Argument<?> argument) {
        return argument.findAnnotation(MqttProperty.class)
                .flatMap(AnnotationValue::stringValue)
                .orElse(argument.getName());
    }

}
