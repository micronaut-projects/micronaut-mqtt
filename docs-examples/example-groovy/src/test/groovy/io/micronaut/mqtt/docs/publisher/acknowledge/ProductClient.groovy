package io.micronaut.mqtt.docs.publisher.acknowledge



// tag::imports[]
import io.micronaut.mqtt.annotation.Topic
import io.micronaut.mqtt.v5.annotation.MqttPublisher
import io.reactivex.Completable
import io.reactivex.Maybe
import org.reactivestreams.Publisher
import reactor.core.publisher.Mono
// end::imports[]

import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "PublisherAcknowledgeSpec")
// tag::clazz[]
@MqttPublisher
interface ProductClient {

    @Topic("product")
    Completable send(byte[] data) // <1>

    @Topic("product")
    Maybe<Void> sendMaybe(byte[] data) // <2>

    @Topic("product")
    Mono<Void> sendMono(byte[] data) // <3>

    @Topic("product")
    Publisher<Void> sendPublisher(byte[] data) // <4>
}
// end::clazz[]
