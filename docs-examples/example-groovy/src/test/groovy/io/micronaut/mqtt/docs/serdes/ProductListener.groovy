package io.micronaut.mqtt.docs.serdes

// tag::imports[]
import io.micronaut.mqtt.annotation.MqttSubscriber
import io.micronaut.mqtt.annotation.Topic
// end::imports[]

import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "ProductInfoSerDesSpec")
// tag::clazz[]
@MqttSubscriber
class ProductListener {

    List<ProductInfo> messages = Collections.synchronizedList([])

    @Topic("product")
    void receive(ProductInfo productInfo) { // <1>
        messages.add(productInfo)
    }
}
// end::clazz[]
