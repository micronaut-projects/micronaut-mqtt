package io.micronaut.mqtt.docs.parameters

// tag::imports[]
import io.micronaut.mqtt.v5.annotation.MqttPublisher
import io.micronaut.context.annotation.Requires
import io.micronaut.mqtt.annotation.Topic
// end::imports[]

@Requires(property = "spec.name", value = "BindingSpec")
// tag::clazz[]
@MqttPublisher
interface ProductClient {

    @Topic("product") // <1>
    fun send(data: ByteArray)

    fun send(@Topic binding: String, data: ByteArray)  // <2>
}
// end::clazz[]
