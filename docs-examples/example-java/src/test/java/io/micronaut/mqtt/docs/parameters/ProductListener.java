package io.micronaut.mqtt.docs.parameters;

// tag::imports[]
import io.micronaut.mqtt.annotation.Topic;
import io.micronaut.mqtt.annotation.MqttSubscriber;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
// end::imports[]

import io.micronaut.context.annotation.Requires;

@Requires(property = "spec.name", value = "BindingSpec")
// tag::clazz[]
@MqttSubscriber
public class ProductListener {

    List<Integer> messageLengths = Collections.synchronizedList(new ArrayList<>());
    List<String> topics = Collections.synchronizedList(new ArrayList<>());

    @Topic("product") // <1>
    public void receive(byte[] data) {
        messageLengths.add(data.length);
    }

    @Topic("product/a")
    @Topic("product/b") // <2>
    public void receive(byte[] data, @Topic String topic) {
        topics.add(topic);
    }
}
// end::clazz[]
