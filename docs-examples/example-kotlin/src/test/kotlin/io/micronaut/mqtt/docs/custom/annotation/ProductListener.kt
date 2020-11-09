package io.micronaut.mqtt.docs.custom.annotation

// tag::imports[]
import io.micronaut.mqtt.annotation.MqttSubscriber
import io.micronaut.context.annotation.Requires
import io.micronaut.mqtt.annotation.Topic
import java.nio.charset.StandardCharsets

import java.util.Collections
import java.util.HashSet
// end::imports[]

@Requires(property = "spec.name", value = "CorrelationSpec")
// tag::clazz[]
@MqttSubscriber
class ProductListener {

    val messages: MutableSet<String> = Collections.synchronizedSet(HashSet())

    @Topic("product")
    fun receive(@Correlation data: ByteArray) { // <1>
        messages.add(String(data, StandardCharsets.UTF_8))
    }
}
// end::clazz[]
