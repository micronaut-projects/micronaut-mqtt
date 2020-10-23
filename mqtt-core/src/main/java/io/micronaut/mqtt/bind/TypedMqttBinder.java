package io.micronaut.mqtt.bind;

import io.micronaut.core.type.Argument;

interface TypedMqttBinder<M, T> extends MqttBinder<M, T> {

    Argument<T> getArgumentType();

}
