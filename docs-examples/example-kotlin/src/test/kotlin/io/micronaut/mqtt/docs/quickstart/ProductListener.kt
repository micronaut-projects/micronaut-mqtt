package io.micronaut.mqtt.docs.quickstart

// tag::imports[]
import io.micronaut.context.annotation.Requires
import io.micronaut.mqtt.annotation.MqttSubscriber
import io.micronaut.mqtt.annotation.Topic
import java.util.*
// end::imports[]

@Requires(property = "spec.name", value = "QuickstartSpec")
// tag::clazz[]
@MqttSubscriber // <1>
class ProductListener {

    val messageLengths: MutableList<String> = Collections.synchronizedList(ArrayList())

    @Topic("product") // <2>
    fun receive(data: ByteArray) { // <3>
        val string = String(data)
        messageLengths.add(string)
        println("Kotlin received ${data.size} bytes from MQTT: ${string}")
    }
}
// end::clazz[]
