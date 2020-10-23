package io.micronaut.mqtt.bind;

import io.micronaut.core.type.Argument;

import java.util.Optional;

public interface MqttBinderRegistry {

    <T> Optional<MqttBinder<?, T>> findArgumentBinder(Argument<T> argument);
}
