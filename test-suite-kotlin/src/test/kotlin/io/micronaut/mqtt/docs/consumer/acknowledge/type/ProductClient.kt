package io.micronaut.mqtt.docs.consumer.acknowledge.type

// tag::imports[]
import io.micronaut.mqtt.annotation.Topic
import io.micronaut.mqtt.annotation.v5.MqttPublisher
// end::imports[]

import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "AcknowledgeSpec")
// tag::clazz[]
@MqttPublisher // <1>
interface ProductClient {

    @Topic("product") // <2>
    fun send(data: ByteArray)  // <3>
}
// end::clazz[]
