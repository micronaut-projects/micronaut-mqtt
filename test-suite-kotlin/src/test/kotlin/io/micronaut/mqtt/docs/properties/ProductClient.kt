package io.micronaut.mqtt.docs.properties

// tag::imports[]
import io.micronaut.mqtt.annotation.v5.MqttPublisher
import io.micronaut.mqtt.annotation.Topic
import io.micronaut.mqtt.annotation.v5.MqttProperties
import io.micronaut.mqtt.annotation.v5.MqttProperty
// end::imports[]

import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "PropertiesSpec")
// tag::clazz[]
@MqttPublisher
@MqttProperty(name = "appId", value = "myApp") // <1>
interface ProductClient {

    @Topic("product")
    @MqttProperties( // <2>
            MqttProperty(name = "contentType", value = "application/json"),
            MqttProperty(name = "userId", value = "guest")
    )
    fun send(data: ByteArray)

    @Topic("product")
    fun send(@MqttProperty("userId") user: String, @MqttProperty contentType: String?, data: ByteArray)  // <3>
}
// end::clazz[]
