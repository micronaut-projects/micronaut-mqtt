package io.micronaut.mqtt.docs.custom.annotation

import io.micronaut.mqtt.v5.annotation.MqttPublisher
import io.micronaut.context.annotation.Requires
import io.micronaut.mqtt.annotation.Topic

@Requires(property = "spec.name", value = "CorrelationSpec")
@MqttPublisher
interface ProductClient {

    @Topic("product")
    fun send(@Correlation data: ByteArray)
}
