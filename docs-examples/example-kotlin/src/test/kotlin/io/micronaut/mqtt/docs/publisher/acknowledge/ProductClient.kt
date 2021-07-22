package io.micronaut.mqtt.docs.publisher.acknowledge

// tag::imports[]
import io.micronaut.mqtt.v5.annotation.MqttPublisher
import io.micronaut.mqtt.annotation.Topic
import org.reactivestreams.Publisher
// end::imports[]

import io.micronaut.context.annotation.Requires
import java.util.concurrent.CompletableFuture

@Requires(property = "spec.name", value = "PublisherAcknowledgeSpec")
// tag::clazz[]
@MqttPublisher
interface ProductClient {

    @Topic("product")
    fun sendPublisher(data: ByteArray): Publisher<Void>  // <1>

    @Topic("product")
    fun sendFuture(data: ByteArray): CompletableFuture<Void>  // <2>

    @Topic("product")
    suspend fun sendSuspend(data: ByteArray): Unit //suspend methods work too!
}
// end::clazz[]
