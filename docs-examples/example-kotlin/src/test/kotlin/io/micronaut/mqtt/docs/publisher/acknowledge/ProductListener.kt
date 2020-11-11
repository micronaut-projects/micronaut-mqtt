package io.micronaut.mqtt.docs.publisher.acknowledge

// tag::imports[]
import io.micronaut.context.annotation.Requires
import io.micronaut.mqtt.annotation.MqttSubscriber
import io.micronaut.mqtt.annotation.Topic
import java.util.*
// end::imports[]

@Requires(property = "spec.name", value = "PublisherAcknowledgeSpec")
// tag::clazz[]
@MqttSubscriber // <1>
class ProductListener {

    val messageLengths: MutableList<Int> = Collections.synchronizedList(ArrayList())

    @Topic("product") // <2>
    fun receive(data: ByteArray) { // <3>
        val length = data.size
        messageLengths.add(length)
        println("Kotlin received $length bytes from MQTT")
    }
}
// end::clazz[]
