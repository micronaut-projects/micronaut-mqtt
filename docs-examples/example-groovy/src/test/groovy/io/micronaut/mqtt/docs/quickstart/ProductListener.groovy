package io.micronaut.mqtt.docs.quickstart

// tag::imports[]
import io.micronaut.context.annotation.Requires
import io.micronaut.mqtt.annotation.MqttSubscriber
import io.micronaut.mqtt.annotation.Topic
// end::imports[]

@Requires(property = "spec.name", value = "QuickstartSpec")
// tag::clazz[]
@MqttSubscriber // <1>
class ProductListener {

    List<String> messageLengths = Collections.synchronizedList([])

    @Topic("product") // <2>
    void receive(byte[] data) { // <3>
        messageLengths.add(new String(data))
        println("Groovy received ${data.length} bytes from MQTT")
    }
}
// end::clazz[]
