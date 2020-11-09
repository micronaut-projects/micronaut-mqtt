package io.micronaut.mqtt.docs.serdes;

// tag::imports[]
import io.micronaut.mqtt.annotation.Topic;
import io.micronaut.mqtt.v5.annotation.MqttPublisher;
import io.micronaut.context.annotation.Requires;
import io.micronaut.messaging.annotation.Body;
// end::imports[]

@Requires(property = "spec.name", value = "ProductInfoSerDesSpec")
// tag::clazz[]
@MqttPublisher
public interface ProductClient {

    @Topic("product")
    void send(@Body ProductInfo data);
}
// end::clazz[]
