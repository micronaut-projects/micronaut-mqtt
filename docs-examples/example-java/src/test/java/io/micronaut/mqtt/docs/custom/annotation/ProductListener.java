package io.micronaut.mqtt.docs.custom.annotation;

// tag::imports[]
import io.micronaut.mqtt.annotation.MqttSubscriber;
import io.micronaut.context.annotation.Requires;
import io.micronaut.mqtt.annotation.Topic;

import java.util.*;
// end::imports[]

@Requires(property = "spec.name", value = "CorrelationSpec")
// tag::clazz[]
@MqttSubscriber
public class ProductListener {

    Set<String> messages = Collections.synchronizedSet(new HashSet<>());

    @Topic("product")
    public void receive(byte[] data, @Correlation byte[] correlation) { // <1>
        messages.add(new String(correlation));
    }
}
// end::clazz[]
