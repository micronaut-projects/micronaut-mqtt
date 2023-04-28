package io.micronaut.mqtt.docs.parameters;

// tag::imports[]
import io.micronaut.mqtt.annotation.Topic;
import io.micronaut.mqtt.annotation.v5.MqttPublisher;
// end::imports[]

import io.micronaut.context.annotation.Requires;

@Requires(property = "spec.name", value = "BindingSpec")
// tag::clazz[]
@MqttPublisher
public interface ProductClient {

    @Topic("product") // <1>
    void send(byte[] data);

    void send(@Topic String binding, byte[] data); // <2>
}
// end::clazz[]
