package io.micronaut.mqtt.docs.properties

// tag::imports[]
import io.micronaut.core.annotation.Nullable

import io.micronaut.mqtt.annotation.MqttSubscriber
import io.micronaut.mqtt.annotation.Topic
import io.micronaut.mqtt.annotation.v5.MqttProperty
// end::imports[]
import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "PropertiesSpec")
// tag::clazz[]
@MqttSubscriber
class ProductListener {

    List<String> messageProperties = Collections.synchronizedList([])

    @Topic("product")
    void receive(byte[] data,
                 @MqttProperty("userId") String user,  // <1>
                 @Nullable @MqttProperty String contentType,  // <2>
                 @MqttProperty String appId) {  // <3>
        messageProperties.add(user + "|" + contentType + "|" + appId)
    }
}
// end::clazz[]
