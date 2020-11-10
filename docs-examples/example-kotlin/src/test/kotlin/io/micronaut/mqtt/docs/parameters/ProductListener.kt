package io.micronaut.mqtt.docs.parameters

// tag::imports[]
import io.micronaut.mqtt.annotation.Topic
import io.micronaut.mqtt.annotation.MqttSubscriber
import java.util.*
import kotlin.collections.ArrayList
// end::imports[]

import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "BindingSpec")
// tag::clazz[]
@MqttSubscriber
class ProductListener {

    val messageLengths: MutableList<Int> = Collections.synchronizedList(ArrayList())

    @Topic("product") // <1>
    fun receive(data: ByteArray) {
        messageLengths.add(data.size)
    }
}
// end::clazz[]
