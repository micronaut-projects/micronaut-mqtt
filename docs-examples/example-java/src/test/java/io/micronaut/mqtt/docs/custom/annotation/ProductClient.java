package io.micronaut.mqtt.docs.custom.annotation;

import io.micronaut.mqtt.annotation.Topic;
import io.micronaut.mqtt.v5.annotation.MqttPublisher;
import io.micronaut.context.annotation.Requires;

@Requires(property = "spec.name", value = "CorrelationSpec")
@MqttPublisher
public interface ProductClient {

    @Topic("product")
    void send(byte[] data, @Correlation byte[] correlation);
}
