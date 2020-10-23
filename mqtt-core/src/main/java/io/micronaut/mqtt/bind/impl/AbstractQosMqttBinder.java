package io.micronaut.mqtt.bind.impl;

import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.mqtt.annotation.Qos;
import io.micronaut.mqtt.bind.AnnotatedMqttBinder;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class AbstractQosMqttBinder<T> implements AnnotatedMqttBinder<T, Qos> {

    private final ConversionService<?> conversionService;
    private final Function<T, Integer> function;
    private final BiConsumer<T, Integer> consumer;

    public AbstractQosMqttBinder(ConversionService<?> conversionService,
                                 Function<T, Integer> function,
                                 BiConsumer<T, Integer> consumer) {
        this.conversionService = conversionService;
        this.function = function;
        this.consumer = consumer;
    }

    @Override
    public Class<Qos> getAnnotationType() {
        return Qos.class;
    }

    @Override
    public void bindTo(T message, Object value, Argument<?> argument) {
        conversionService.convert(value, Argument.INT)
                .ifPresent((val) -> consumer.accept(message, val));
    }

    @Override
    public Optional<?> bindFrom(T message, Argument<?> argument) {
        return conversionService.convert(function.apply(message), argument);
    }
}
