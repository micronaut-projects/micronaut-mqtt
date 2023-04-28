package io.micronaut.mqtt.bind.topic

import io.micronaut.context.annotation.Requires
import io.micronaut.mqtt.test.bind.topic.TopicBindingClient
import io.micronaut.mqtt.test.bind.topic.TopicBindingSpec
import io.micronaut.mqtt.annotation.v3.MqttPublisher

class V3TopicBindingSpec extends TopicBindingSpec {

    @Override
    Class<? extends TopicBindingClient> getClient() {
        return MyClient.class
    }

    @Requires(property = "spec.name", value = "V3TopicBindingSpec")
    @MqttPublisher
    static interface MyClient extends TopicBindingClient {}
}
