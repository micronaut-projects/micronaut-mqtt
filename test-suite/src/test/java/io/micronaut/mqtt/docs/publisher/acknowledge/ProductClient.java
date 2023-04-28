package io.micronaut.mqtt.docs.publisher.acknowledge;

// tag::imports[]
import io.micronaut.mqtt.annotation.Topic;
import io.micronaut.mqtt.annotation.v5.MqttPublisher;
import org.reactivestreams.Publisher;
// end::imports[]

import io.micronaut.context.annotation.Requires;

import java.util.concurrent.CompletableFuture;

@Requires(property = "spec.name", value = "PublisherAcknowledgeSpec")
// tag::clazz[]
@MqttPublisher
public interface ProductClient {

    @Topic("product")
    Publisher<Void> sendPublisher(byte[] data); // <1>

    @Topic("product")
    CompletableFuture<Void> sendFuture(byte[] data); // <2>
}
// end::clazz[]
