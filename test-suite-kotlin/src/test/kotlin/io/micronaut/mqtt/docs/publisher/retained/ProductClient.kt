package io.micronaut.mqtt.docs.publisher.retained

// tag::imports[]
import io.micronaut.mqtt.annotation.Retained
import io.micronaut.mqtt.annotation.Topic
import io.micronaut.mqtt.annotation.v5.MqttPublisher
// end::imports[]

import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "PublisherQosSpec")

// tag::clazz[]
@MqttPublisher
interface ProductClient {

    @Topic("product")
    @Retained(true) // <1>
    fun send(data: ByteArray?)

    @Topic("product")
    fun send(data: ByteArray?, @Retained retained: Boolean) // <2>
}
// end::clazz[]
