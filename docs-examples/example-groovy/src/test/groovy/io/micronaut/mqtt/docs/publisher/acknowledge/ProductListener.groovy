package io.micronaut.mqtt.docs.publisher.acknowledge

// tag::imports[]
import io.micronaut.context.annotation.Requires
import io.micronaut.mqtt.annotation.MqttSubscriber
import io.micronaut.mqtt.annotation.Topic
// end::imports[]

@Requires(property = "spec.name", value = "PublisherAcknowledgeSpec")
// tag::clazz[]
@MqttSubscriber // <1>
class ProductListener {

    List<Integer> messageLengths = Collections.synchronizedList([])

    @Topic("product") // <2>
    void receive(byte[] data) { // <3>
        Integer length = data.length
        messageLengths.add(length)
        println("Groovy received " + length + " bytes from MQTT")
    }
}
// end::clazz[]
