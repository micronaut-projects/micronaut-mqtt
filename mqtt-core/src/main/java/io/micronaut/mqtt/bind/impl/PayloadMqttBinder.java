package io.micronaut.mqtt.bind.impl;

import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.type.Argument;
import io.micronaut.messaging.annotation.Body;
import io.micronaut.mqtt.bind.AnnotatedMqttBinder;
import io.micronaut.mqtt.bind.MqttBindingContext;
import io.micronaut.mqtt.serdes.MqttPayloadSerDes;
import io.micronaut.mqtt.serdes.MqttPayloadSerDesRegistry;

import javax.inject.Singleton;
import java.util.Optional;

@Singleton
public class PayloadMqttBinder implements AnnotatedMqttBinder<MqttBindingContext<?>, Body> {

    private final MqttPayloadSerDesRegistry serDesRegistry;

    public PayloadMqttBinder(MqttPayloadSerDesRegistry serDesRegistry) {
        this.serDesRegistry = serDesRegistry;
    }

    @Override
    public Class<Body> getAnnotationType() {
        return Body.class;
    }

    @Override
    public void bindTo(MqttBindingContext<?> context, Object value, Argument<?> argument) {
        serDesRegistry.findSerdes(argument)
                .map(serDes -> ((MqttPayloadSerDes<Object>) serDes).serialize(value))
                .ifPresent(context::setPayload);
    }

    @Override
    public Optional<?> bindFrom(MqttBindingContext<?> context, ArgumentConversionContext<?> conversionContext) {
        return serDesRegistry.findSerdes(conversionContext.getArgument())
                .map(serDes -> ((MqttPayloadSerDes<Object>) serDes).deserialize(context.getPayload(), (Argument<Object>) conversionContext.getArgument()));
    }
}
