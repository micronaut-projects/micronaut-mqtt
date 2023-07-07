package io.micronaut.mqtt.hivemq.v3.intercept

import io.micronaut.context.annotation.Requires
import io.micronaut.mqtt.annotation.v3.MqttPublisher
import io.micronaut.mqtt.test.MQTT3Test
import io.micronaut.mqtt.test.intercept.SimplePubSubClient
import io.micronaut.mqtt.test.intercept.SimplePubSubSpec

class SimpleV3PubSubSpec extends SimplePubSubSpec implements MQTT3Test {

    @Override
    Class<? extends SimplePubSubClient> getClient() {
        return SimpleV3PubSubClient.class
    }

    @Requires(property = "spec.name", value = "SimpleV3PubSubSpec")
    @MqttPublisher
    static interface SimpleV3PubSubClient extends SimplePubSubClient {}
}
