package io.micronaut.mqtt.docs.quickstart

// tag::imports[]
import io.micronaut.mqtt.v5.annotation.MqttPublisher
import io.micronaut.context.annotation.Requires
import io.micronaut.mqtt.annotation.Topic
// end::imports[]

@Requires(property = "spec.name", value = "QuickstartSpec")
// tag::clazz[]
@MqttPublisher // <1>
interface ProductClient {

    @Topic("product") // <2>
    fun send(data: ByteArray)  // <3>
}
// end::clazz[]
