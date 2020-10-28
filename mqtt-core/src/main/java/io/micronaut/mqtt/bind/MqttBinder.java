package io.micronaut.mqtt.bind;

import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.type.Argument;

import java.util.Optional;

public interface MqttBinder<M, T> {

    void bindTo(M context, T value, Argument<?> argument);

    Optional<?> bindFrom(M context, ArgumentConversionContext<?> conversionContext);
}
