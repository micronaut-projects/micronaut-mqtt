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
package io.micronaut.mqtt.hivemq.v5.bind;

import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.beans.BeanProperty;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.mqtt.annotation.v5.MqttProperty;
import io.micronaut.mqtt.bind.AnnotatedMqttBinder;
import io.micronaut.mqtt.hivemq.bind.MqttMessage;
import io.micronaut.mqtt.hivemq.bind.UserProperty;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.Optional;

/**
 * Binds arguments to and from {@link MqttMessage}.
 *
 * @author Sven Kobow
 * @since 3.0.0
 */
@Introspected(classes = MqttMessage.class)
@Singleton
public class MqttPropertyBinder implements AnnotatedMqttBinder<MqttV5BindingContext, MqttProperty> {

    private final ConversionService conversionService;
    private final BeanIntrospection<MqttMessage> introspection;

    /**
     * @param conversionService The conversion service
     */
    public MqttPropertyBinder(ConversionService conversionService) {
        this.conversionService = conversionService;
        this.introspection = BeanIntrospection.getIntrospection(MqttMessage.class);
    }

    @Override
    public Class<MqttProperty> getAnnotationType() {
        return MqttProperty.class;
    }

    @Override
    public void bindTo(MqttV5BindingContext context, Object value, Argument<Object> argument) {
        String name = getParameterName(argument);
        Optional<BeanProperty<MqttMessage, Object>> property = introspection.getProperty(name);
        if (property.isPresent()) {
            property.get().convertAndSet(context.getNativeMessage(), value);
        } else {
            List<UserProperty> userProperties = context.getNativeMessage().getUserProperties();
            userProperties.removeIf(up -> up.getKey().equals(name));
            conversionService.convert(value, Argument.STRING)
                .map(val -> new UserProperty(name, val))
                .ifPresent(userProperties::add);
        }
    }

    @Override
    public Optional<Object> bindFrom(MqttV5BindingContext context, ArgumentConversionContext<Object> conversionContext) {
        String name = getParameterName(conversionContext.getArgument());
        Optional<BeanProperty<MqttMessage, Object>> property = introspection.getProperty(name);
        if (property.isPresent()) {
            return property.get().get(context.getNativeMessage(), conversionContext);
        } else {
            List<UserProperty> userProperties = context.getNativeMessage().getUserProperties();
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
