package io.micronaut.mqtt.bind.impl;

import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.mqtt.annotation.Retained;
import io.micronaut.mqtt.bind.AnnotatedMqttBinder;
import io.micronaut.mqtt.bind.MqttBindingContext;

import javax.inject.Singleton;
import java.util.Optional;

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
    public void bindTo(MqttBindingContext<?> message, Object value, Argument<?> argument) {
        conversionService.convert(value, Argument.BOOLEAN)
                .ifPresent(message::setRetained);
    }

    @Override
    public Optional<?> bindFrom(MqttBindingContext<?> message, ArgumentConversionContext<?> conversionContext) {
        return conversionService.convert(message.isRetained(), conversionContext);
    }
}
