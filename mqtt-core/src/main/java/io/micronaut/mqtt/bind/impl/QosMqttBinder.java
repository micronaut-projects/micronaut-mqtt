package io.micronaut.mqtt.bind.impl;

import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.mqtt.annotation.Qos;
import io.micronaut.mqtt.bind.AnnotatedMqttBinder;
import io.micronaut.mqtt.bind.MqttBindingContext;

import javax.inject.Singleton;
import java.util.Optional;

@Singleton
public class QosMqttBinder implements AnnotatedMqttBinder<MqttBindingContext<?>, Qos> {

    private final ConversionService<?> conversionService;

    public QosMqttBinder(ConversionService<?> conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public Class<Qos> getAnnotationType() {
        return Qos.class;
    }

    @Override
    public void bindTo(MqttBindingContext<?> context, Object value, Argument<?> argument) {
        conversionService.convert(value, Argument.INT)
                .ifPresent(context::setQos);
    }

    @Override
    public Optional<?> bindFrom(MqttBindingContext<?> context, ArgumentConversionContext<?> conversionContext) {
        return conversionService.convert(context.getQos(), conversionContext);
    }
}
