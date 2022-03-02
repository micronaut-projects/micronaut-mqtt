package io.micronaut.mqtt.docs.publisher.retained;

// tag::imports[]
import io.micronaut.mqtt.annotation.Retained;
import io.micronaut.mqtt.annotation.Topic;
import io.micronaut.mqtt.v5.annotation.MqttPublisher;
// end::imports[]

import io.micronaut.context.annotation.Requires;

@Requires(property = "spec.name", value = "PublisherQosSpec")
// tag::clazz[]
@MqttPublisher
public interface ProductClient {

    @Topic("product")
    @Retained(true)// <1>
    void send(byte[] data);

    @Topic("product")
    void send(byte[] data, @Retained boolean retained); // <2>
}
// end::clazz[]
