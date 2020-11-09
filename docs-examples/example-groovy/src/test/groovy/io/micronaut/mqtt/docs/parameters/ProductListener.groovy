package io.micronaut.mqtt.docs.parameters

// tag::imports[]
import io.micronaut.mqtt.annotation.MqttSubscriber
import io.micronaut.mqtt.annotation.Topic
// end::imports[]

import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "BindingSpec")
// tag::clazz[]
@MqttSubscriber
class ProductListener {

    List<Integer> messageLengths = Collections.synchronizedList([])

    @Topic("product") // <1>
    void receive(byte[] data) {
        messageLengths.add(data.length)
    }
}
// end::clazz[]
