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

    @Topic(value = "product") // <1>
    void receive(byte[] data, Acknowledgement acknowledgement) { // <2>
        messageCount.getAndUpdate({ intValue -> ++intValue })
        println new String(data)
        acknowledgement
    }
}
// end::clazz[]
