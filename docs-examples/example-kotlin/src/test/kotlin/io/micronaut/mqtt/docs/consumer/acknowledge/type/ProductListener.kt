package io.micronaut.mqtt.docs.consumer.acknowledge.type

// tag::imports[]
import io.micronaut.mqtt.annotation.Topic
import io.micronaut.mqtt.annotation.MqttSubscriber
import io.micronaut.messaging.Acknowledgement

import java.util.concurrent.atomic.AtomicInteger
// end::imports[]

import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "AcknowledgeSpec")
// tag::clazz[]
@MqttSubscriber
class ProductListener {

    val messageCount = AtomicInteger()

    @Topic("product")
    fun receive(data: ByteArray, acknowledgement: Acknowledgement) { // <1>
        messageCount.getAndUpdate { intValue -> intValue + 1 }
        acknowledgement.ack() // <2>
    }
}
// end::clazz[]
