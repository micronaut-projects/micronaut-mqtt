package io.micronaut.mqtt.docs.custom.type

// tag::imports[]
import io.micronaut.mqtt.annotation.Topic
import io.micronaut.mqtt.v5.annotation.MqttPublisher
// end::imports[]

import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "ProductInfoSpec")
// tag::clazz[]
@MqttPublisher
interface ProductClient {

    @Topic("product")
    void send(byte[] data, ProductInfo productInfo)
}
// end::clazz[]
