package io.micronaut.mqtt.bind;

import io.micronaut.core.type.Argument;

public interface MqttBinder<M, T> {

    void bindTo(M message, T value, Argument<?> argument);

    T bindFrom(M message, Argument<?> argument);
}
