package io.micronaut.mqtt.docs.consumer.acknowledge.type;

// tag::imports[]
import io.micronaut.mqtt.annotation.Topic;
import io.micronaut.mqtt.v5.annotation.MqttPublisher;
import io.micronaut.context.annotation.Requires;
// end::imports[]

@Requires(property = "spec.name", value = "AcknowledgeSpec")
// tag::clazz[]
@MqttPublisher // <1>
public interface ProductClient {

    @Topic("product") // <2>
    void send(byte[] data); // <3>
}
// end::clazz[]
