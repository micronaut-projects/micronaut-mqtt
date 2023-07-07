package io.micronaut.mqtt.intercept

import io.micronaut.context.annotation.Requires
import io.micronaut.mqtt.annotation.v5.MqttPublisher
import io.micronaut.mqtt.test.MQTT5Test
import io.micronaut.mqtt.test.intercept.MultipleTopicsClient
import io.micronaut.mqtt.test.intercept.MultipleTopicsSpec

class V5MultipleTopicsSpec extends MultipleTopicsSpec implements MQTT5Test {

    @Override
    Class<? extends MultipleTopicsClient> getClient() {
        return V3MultipleTopicsClient
    }

    @Requires(property = "spec.name", value = "V5MultipleTopicsSpec")
    @MqttPublisher
    static interface V3MultipleTopicsClient extends MultipleTopicsClient {}
}
