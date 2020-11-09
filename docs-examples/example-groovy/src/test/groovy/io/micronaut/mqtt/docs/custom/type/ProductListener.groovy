package io.micronaut.mqtt.docs.custom.type

// tag::imports[]
import io.micronaut.mqtt.annotation.MqttSubscriber
import io.micronaut.mqtt.annotation.Topic
// end::imports[]

import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "ProductInfoSpec")
// tag::clazz[]
@MqttSubscriber
class ProductListener {

    List<ProductInfo> messages = Collections.synchronizedList([])

    @Topic("product")
    void receive(byte[] data,
                 ProductInfo productInfo) { // <1>
        messages.add(productInfo)
    }
}
// end::clazz[]
