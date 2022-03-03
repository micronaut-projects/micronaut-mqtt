package io.micronaut.mqtt.docs.quickstart;

// tag::imports[]
import io.micronaut.mqtt.annotation.Topic;
import io.micronaut.mqtt.v5.annotation.MqttPublisher;
// end::imports[]

import io.micronaut.context.annotation.Requires;

@Requires(property = "spec.name", value = "QuickstartSpec")
// tag::clazz[]
@MqttPublisher // <1>
public interface ProductClient {

    @Topic("product") // <2>
    void send(byte[] data); // <3>
}
// end::clazz[]
