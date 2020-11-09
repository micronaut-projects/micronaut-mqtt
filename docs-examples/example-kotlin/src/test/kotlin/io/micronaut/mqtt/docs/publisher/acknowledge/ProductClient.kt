package io.micronaut.mqtt.docs.publisher.acknowledge

// tag::imports[]
import io.micronaut.mqtt.v5.annotation.MqttPublisher
import io.micronaut.context.annotation.Requires
import io.micronaut.mqtt.annotation.Topic
import io.reactivex.Completable
import io.reactivex.Maybe
import org.reactivestreams.Publisher
import reactor.core.publisher.Mono
// end::imports[]

@Requires(property = "spec.name", value = "PublisherAcknowledgeSpec")
// tag::clazz[]
@MqttPublisher
interface ProductClient {

    @Topic("product")
    fun send(data: ByteArray): Completable  // <1>

    @Topic("product")
    fun sendMaybe(data: ByteArray): Maybe<Void>  // <2>

    @Topic("product")
    fun sendMono(data: ByteArray): Mono<Void>  // <3>

    @Topic("product")
    fun sendPublisher(data: ByteArray): Publisher<Void>  // <4>
}
// end::clazz[]
