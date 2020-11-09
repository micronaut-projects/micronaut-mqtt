package io.micronaut.mqtt.docs.custom.type;

// tag::imports[]
import io.micronaut.mqtt.annotation.Topic;
import io.micronaut.mqtt.v5.annotation.MqttPublisher;
import io.micronaut.context.annotation.Requires;
// end::imports[]

@Requires(property = "spec.name", value = "ProductInfoSpec")
// tag::clazz[]
@MqttPublisher
public interface ProductClient {

    @Topic("product")
    void send(byte[] data, ProductInfo productInfo);
}
// end::clazz[]
