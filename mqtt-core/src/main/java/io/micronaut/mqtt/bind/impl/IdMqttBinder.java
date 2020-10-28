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
    public void bindTo(MqttBindingContext<?> context, Object value, Argument<?> argument) {
        throw new MqttClientException("The message ID cannot be set when publishing messages");
    }

    @Override
    public Optional<?> bindFrom(MqttBindingContext<?> context, ArgumentConversionContext<?> conversionContext) {
        return conversionService.convert(context.getId(), conversionContext);
    }
}
