package io.micronaut.mqtt.docs.publisher.qos;

// tag::imports[]
import io.micronaut.mqtt.annotation.Qos;
import io.micronaut.mqtt.annotation.Topic;
import io.micronaut.mqtt.v5.annotation.MqttPublisher;
// end::imports[]

import io.micronaut.context.annotation.Requires;

@Requires(property = "spec.name", value = "PublisherQosSpec")
// tag::clazz[]
@MqttPublisher
public interface ProductClient {

    @Topic(value = "product", qos = 2) // <1>
    void send(byte[] data);

    @Topic("product")
    void send(byte[] data, @Qos int qos); // <2>
}
// end::clazz[]
