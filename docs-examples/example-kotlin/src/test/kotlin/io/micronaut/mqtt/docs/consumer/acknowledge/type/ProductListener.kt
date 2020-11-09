package io.micronaut.mqtt.docs.consumer.acknowledge.type

// tag::imports[]
import io.micronaut.mqtt.annotation.Topic
import io.micronaut.mqtt.annotation.MqttSubscriber
import io.micronaut.messaging.Acknowledgement
import io.micronaut.context.annotation.Requires

import java.util.concurrent.atomic.AtomicInteger
// end::imports[]

@Requires(property = "spec.name", value = "AcknowledgeSpec")
// tag::clazz[]
@MqttSubscriber
class ProductListener {

    val messageCount = AtomicInteger()

    @Topic(value = "product") // <1>
    fun receive(data: ByteArray, acknowledgement: Acknowledgement) { // <2>
        messageCount.getAndUpdate { intValue -> intValue + 1 }
        acknowledgement.ack() // <4>
    }
}
// end::clazz[]
