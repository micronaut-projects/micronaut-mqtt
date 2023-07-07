package io.micronaut.mqtt.bind.topic

import io.micronaut.context.annotation.Requires
import io.micronaut.mqtt.test.MQTT5Test
import io.micronaut.mqtt.test.bind.topic.TopicBindingClient
import io.micronaut.mqtt.test.bind.topic.TopicBindingSpec
import io.micronaut.mqtt.annotation.v5.MqttPublisher

class V5TopicBindingSpec extends TopicBindingSpec implements MQTT5Test {

    @Override
    Class<? extends TopicBindingClient> getClient() {
        return MyClient.class
    }

    @Requires(property = "spec.name", value = "V5TopicBindingSpec")
    @MqttPublisher
    static interface MyClient extends TopicBindingClient {}
}
