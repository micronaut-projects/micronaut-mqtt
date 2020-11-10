package io.micronaut.mqtt.docs.properties;

// tag::imports[]
import io.micronaut.mqtt.annotation.Topic;
import io.micronaut.mqtt.annotation.MqttSubscriber;
import io.micronaut.mqtt.v5.annotation.MqttProperty;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
// end::imports[]

import io.micronaut.context.annotation.Requires;

@Requires(property = "spec.name", value = "PropertiesSpec")
// tag::clazz[]
@MqttSubscriber
public class ProductListener {

    List<String> messageProperties = Collections.synchronizedList(new ArrayList<>());

    @Topic("product")
    public void receive(byte[] data,
                        @MqttProperty("userId") String user,  // <2>
                        @Nullable @MqttProperty String contentType,  // <3>
                        @MqttProperty String appId) {  // <4>
        messageProperties.add(user + "|" + contentType + "|" + appId);
    }
}
// end::clazz[]
