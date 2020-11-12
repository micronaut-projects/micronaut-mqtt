package io.micronaut.mqtt.ssl

import io.micronaut.context.annotation.Requires
import io.micronaut.mqtt.annotation.MqttSubscriber
import io.micronaut.mqtt.annotation.Topic

@Requires(property = "spec.name", value = "SslAuthenticationSpec")
@MqttSubscriber // <1>
class ProductListener {

    List<String> messageLengths = Collections.synchronizedList([])

    @Topic("product")
    void receive(byte[] data) {
        messageLengths.add(new String(data))
        println("Groovy received ${data.length} bytes from MQTT")
    }
}
