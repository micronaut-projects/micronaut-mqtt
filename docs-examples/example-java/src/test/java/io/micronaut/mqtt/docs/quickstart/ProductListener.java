package io.micronaut.mqtt.docs.quickstart;

// tag::imports[]
import io.micronaut.mqtt.annotation.Topic;
import io.micronaut.mqtt.annotation.MqttSubscriber;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
// end::imports[]

import io.micronaut.context.annotation.Requires;

@Requires(property = "spec.name", value = "QuickstartSpec")
// tag::clazz[]
@MqttSubscriber // <1>
public class ProductListener {

    List<String> messageLengths = Collections.synchronizedList(new ArrayList<>());

    @Topic("product") // <2>
    public void receive(byte[] data) { // <3>
        messageLengths.add(new String(data));
        System.out.println("Java received " + data.length + " bytes from RabbitMQ");
    }
}
// end::clazz[]
