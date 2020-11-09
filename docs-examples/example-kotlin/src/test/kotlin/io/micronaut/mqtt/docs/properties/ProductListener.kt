package io.micronaut.mqtt.docs.properties

// tag::imports[]
import io.micronaut.mqtt.annotation.Topic
import io.micronaut.mqtt.annotation.MqttSubscriber
import io.micronaut.context.annotation.Requires
import io.micronaut.mqtt.v5.annotation.MqttProperty
import java.util.*
// end::imports[]

@Requires(property = "spec.name", value = "PropertiesSpec")
// tag::clazz[]
@MqttSubscriber
class ProductListener {

    val messageProperties: MutableList<String> = Collections.synchronizedList(ArrayList())

    @Topic("product")
    fun receive(data: ByteArray,
                @MqttProperty("userId") user: String, // <2>
                @MqttProperty contentType: String?, // <3>
                @MqttProperty appId: String) { // <4>
        messageProperties.add("$user|$contentType|$appId")
    }
}
// end::clazz[]
