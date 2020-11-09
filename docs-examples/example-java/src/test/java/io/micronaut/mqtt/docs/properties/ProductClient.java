package io.micronaut.mqtt.docs.properties;

// tag::imports[]
import io.micronaut.mqtt.annotation.Topic;
import io.micronaut.mqtt.v5.annotation.MqttProperty;
import io.micronaut.mqtt.v5.annotation.MqttPublisher;
import io.micronaut.context.annotation.Requires;
// end::imports[]

@Requires(property = "spec.name", value = "PropertiesSpec")
// tag::clazz[]
@MqttPublisher
@MqttProperty(name = "appId", value = "myApp") // <1>
public interface ProductClient {

    @Topic("product")
    @MqttProperty(name = "contentType", value = "application/json") // <2>
    @MqttProperty(name = "userId", value = "guest")
    void send(byte[] data);

    @Topic("product")
    void send(@MqttProperty("userId") String user, @MqttProperty String contentType, byte[] data); // <3>
}
// end::clazz[]
