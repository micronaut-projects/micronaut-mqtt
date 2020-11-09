package io.micronaut.mqtt.docs.properties

// tag::imports[]
import io.micronaut.mqtt.annotation.MqttSubscriber
import io.micronaut.mqtt.annotation.Topic
import io.micronaut.mqtt.v5.annotation.MqttProperty

import javax.annotation.Nullable
// end::imports[]
import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "PropertiesSpec")
// tag::clazz[]
@MqttSubscriber
class ProductListener {

    List<String> messageProperties = Collections.synchronizedList([])

    @Topic("product")
    void receive(byte[] data,
                 @MqttProperty("userId") String user,  // <2>
                 @Nullable @MqttProperty String contentType,  // <3>
                 @MqttProperty String appId) {  // <4>
        messageProperties.add(user + "|" + contentType + "|" + appId)
    }
}
// end::clazz[]
