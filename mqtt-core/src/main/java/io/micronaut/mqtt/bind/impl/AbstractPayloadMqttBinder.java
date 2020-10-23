package io.micronaut.mqtt.bind.impl;

import io.micronaut.core.type.Argument;
import io.micronaut.messaging.annotation.Body;
import io.micronaut.mqtt.annotation.Qos;
import io.micronaut.mqtt.bind.AnnotatedMqttBinder;
import io.micronaut.mqtt.serdes.MqttPayloadSerDes;
import io.micronaut.mqtt.serdes.MqttPayloadSerDesRegistry;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class AbstractPayloadMqttBinder<T> implements AnnotatedMqttBinder<T, Body> {

    private final Function<T, byte[]> function;
    private final BiConsumer<T, byte[]> consumer;
    private final MqttPayloadSerDesRegistry serDesRegistry;

    public AbstractPayloadMqttBinder(MqttPayloadSerDesRegistry serDesRegistry,
                                     Function<T, byte[]> function,
                                     BiConsumer<T, byte[]> consumer) {
        this.serDesRegistry = serDesRegistry;
        this.function = function;
        this.consumer = consumer;
    }

    @Override
    public Class<Body> getAnnotationType() {
        return Body.class;
    }

    @Override
    public void bindTo(T message, Object value, Argument<?> argument) {
        serDesRegistry.findSerdes(argument)
                .map(serDes -> ((MqttPayloadSerDes<Object>) serDes).serialize(value))
                .ifPresent((payload) -> consumer.accept(message, payload));
    }

    @Override
    public Optional<?> bindFrom(T message, Argument<?> argument) {
        return serDesRegistry.findSerdes(argument)
                .map(serDes -> ((MqttPayloadSerDes<Object>) serDes).deserialize(function.apply(message), (Argument<Object>) argument));
    }
}
