package io.micronaut.mqtt.docs.serdes

// tag::imports[]
import io.micronaut.mqtt.annotation.Topic
import io.micronaut.mqtt.annotation.MqttSubscriber

import java.util.ArrayList
import java.util.Collections
// end::imports[]

import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "ProductInfoSerDesSpec")
// tag::clazz[]
@MqttSubscriber
class ProductListener {

    val messages: MutableList<ProductInfo> = Collections.synchronizedList(ArrayList())

    @Topic("product")
    fun receive(productInfo: ProductInfo) { // <1>
        messages.add(productInfo)
    }
}
// end::clazz[]
