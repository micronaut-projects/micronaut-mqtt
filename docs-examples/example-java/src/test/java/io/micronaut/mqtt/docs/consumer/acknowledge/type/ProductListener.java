package io.micronaut.mqtt.docs.consumer.acknowledge.type;

// tag::imports[]
import io.micronaut.mqtt.annotation.Topic;
import io.micronaut.mqtt.annotation.MqttSubscriber;
import io.micronaut.messaging.Acknowledgement;
import io.micronaut.context.annotation.Requires;

import java.util.concurrent.atomic.AtomicInteger;
// end::imports[]

@Requires(property = "spec.name", value = "AcknowledgeSpec")
// tag::clazz[]
@MqttSubscriber
public class ProductListener {

    AtomicInteger messageCount = new AtomicInteger();

    @Topic(value = "product") // <1>
    public void receive(byte[] data, Acknowledgement acknowledgement) { // <2>
        messageCount.getAndUpdate((intValue) -> ++intValue);
        acknowledgement.ack(); // <3>
    }
}
// end::clazz[]
