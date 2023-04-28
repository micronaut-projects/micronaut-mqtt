package io.micronaut.mqtt.intercept

import io.micronaut.context.annotation.Requires
import io.micronaut.mqtt.test.intercept.MultipleTopicsClient
import io.micronaut.mqtt.test.intercept.MultipleTopicsSpec
import io.micronaut.mqtt.annotation.v3.MqttPublisher

class V3MultipleTopicsSpec extends MultipleTopicsSpec {

    @Override
    Class<? extends MultipleTopicsClient> getClient() {
        return V3MultipleTopicsClient
    }

    @Requires(property = "spec.name", value = "V3MultipleTopicsSpec")
    @MqttPublisher
    static interface V3MultipleTopicsClient extends MultipleTopicsClient {}
}
