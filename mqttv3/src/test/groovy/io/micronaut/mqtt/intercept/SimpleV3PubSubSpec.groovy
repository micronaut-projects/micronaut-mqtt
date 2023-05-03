package io.micronaut.mqtt.intercept

import io.micronaut.context.annotation.Requires
import io.micronaut.mqtt.test.intercept.SimplePubSubClient
import io.micronaut.mqtt.test.intercept.SimplePubSubSpec
import io.micronaut.mqtt.annotation.v3.MqttPublisher

class SimpleV3PubSubSpec extends SimplePubSubSpec {

    @Override
    Class<? extends SimplePubSubClient> getClient() {
        return SimpleV3PubSubClient.class
    }

    @Requires(property = "spec.name", value = "SimpleV3PubSubSpec")
    @MqttPublisher
    static interface SimpleV3PubSubClient extends SimplePubSubClient {}
}
