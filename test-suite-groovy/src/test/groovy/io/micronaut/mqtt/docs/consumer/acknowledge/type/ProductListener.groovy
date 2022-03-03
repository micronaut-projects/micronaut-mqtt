package io.micronaut.mqtt.docs.consumer.acknowledge.type

// tag::imports[]
import io.micronaut.messaging.Acknowledgement
import io.micronaut.mqtt.annotation.MqttSubscriber
import io.micronaut.mqtt.annotation.Topic

import java.util.concurrent.atomic.AtomicInteger
// end::imports[]
import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "AcknowledgeSpec")
// tag::clazz[]
@MqttSubscriber
class ProductListener {

    AtomicInteger messageCount = new AtomicInteger()

    @Topic("product")
    void receive(byte[] data, Acknowledgement acknowledgement) { // <1>
        messageCount.getAndUpdate({ intValue -> ++intValue })
        acknowledgement.ack() // <2>
    }
}
// end::clazz[]
