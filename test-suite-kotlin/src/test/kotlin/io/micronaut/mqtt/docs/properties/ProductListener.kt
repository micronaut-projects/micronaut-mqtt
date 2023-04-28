package io.micronaut.mqtt.docs.properties

// tag::imports[]
import io.micronaut.mqtt.annotation.Topic
import io.micronaut.mqtt.annotation.MqttSubscriber
import io.micronaut.mqtt.annotation.v5.MqttProperty
import java.util.*
// end::imports[]

import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "PropertiesSpec")
// tag::clazz[]
@MqttSubscriber
class ProductListener {

    val messageProperties: MutableList<String> = Collections.synchronizedList(ArrayList())

    @Topic("product")
    fun receive(data: ByteArray,
                @MqttProperty("userId") user: String, // <1>
                @MqttProperty contentType: String?, // <2>
                @MqttProperty appId: String) { // <3>
        messageProperties.add("$user|$contentType|$appId")
    }
}
// end::clazz[]
