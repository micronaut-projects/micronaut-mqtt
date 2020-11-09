package io.micronaut.mqtt.docs.custom.type

// tag::imports[]
import io.micronaut.mqtt.v5.annotation.MqttPublisher
import io.micronaut.context.annotation.Requires
import io.micronaut.mqtt.annotation.Topic
// end::imports[]

@Requires(property = "spec.name", value = "ProductInfoSpec")
// tag::clazz[]
@MqttPublisher
interface ProductClient {

    @Topic("product")
    fun send(productInfo: ProductInfo)

}
// end::clazz[]
