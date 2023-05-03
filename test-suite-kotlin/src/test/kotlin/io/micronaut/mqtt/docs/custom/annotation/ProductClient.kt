package io.micronaut.mqtt.docs.custom.annotation

// tag::imports[]
import io.micronaut.mqtt.annotation.v5.MqttPublisher
import io.micronaut.mqtt.annotation.Topic
// end::imports[]

import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "CorrelationSpec")
// tag::clazz[]
@MqttPublisher
interface ProductClient {

    @Topic("product")
    fun send(@Correlation data: ByteArray)
}
// end::clazz[]
