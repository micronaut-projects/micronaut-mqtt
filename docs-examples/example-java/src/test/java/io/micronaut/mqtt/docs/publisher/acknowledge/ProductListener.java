package io.micronaut.mqtt.docs.publisher.acknowledge;

// tag::imports[]
import io.micronaut.mqtt.annotation.MqttSubscriber;
import io.micronaut.mqtt.annotation.Topic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
// end::imports[]
import io.micronaut.context.annotation.Requires;

@Requires(property = "spec.name", value = "PublisherAcknowledgeSpec")
// tag::clazz[]
@MqttSubscriber // <1>
public class ProductListener {

    List<Integer> messageLengths = Collections.synchronizedList(new ArrayList<>());

    @Topic("product") // <2>
    public void receive(byte[] data) { // <3>
        Integer length = data.length;
        messageLengths.add(length);
        System.out.println("Java received " + length + " bytes from MQTT");
    }
}
// end::clazz[]
