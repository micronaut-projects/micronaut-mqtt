package io.micronaut.mqtt.docs.parameters;

// tag::imports[]
import io.micronaut.mqtt.annotation.Topic;
import io.micronaut.mqtt.v5.annotation.MqttPublisher;
import io.micronaut.context.annotation.Requires;
// end::imports[]

@Requires(property = "spec.name", value = "BindingSpec")
// tag::clazz[]
@MqttPublisher
public interface ProductClient {

    @Topic("product") // <1>
    void send(byte[] data);

    void send(@Topic String binding, byte[] data); // <2>
}
// end::clazz[]
