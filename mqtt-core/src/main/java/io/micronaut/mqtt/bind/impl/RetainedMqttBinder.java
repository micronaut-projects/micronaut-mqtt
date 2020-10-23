package io.micronaut.mqtt.bind.impl;

import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.mqtt.annotation.Qos;
import io.micronaut.mqtt.annotation.Retained;
import io.micronaut.mqtt.bind.AnnotatedMqttBinder;
import io.micronaut.mqtt.bind.MqttMessage;

import javax.inject.Singleton;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

@Singleton
public class RetainedMqttBinder implements AnnotatedMqttBinder<MqttMessage, Retained> {

    private final ConversionService<?> conversionService;

    public RetainedMqttBinder(ConversionService<?> conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public Class<Retained> getAnnotationType() {
        return Retained.class;
    }

    @Override
    public void bindTo(MqttMessage message, Object value, Argument<?> argument) {
        conversionService.convert(value, Argument.BOOLEAN)
                .ifPresent(message::setRetained);
    }

    @Override
    public Optional<?> bindFrom(MqttMessage message, Argument<?> argument) {
        return conversionService.convert(message.isRetained(), argument);
    }
}
