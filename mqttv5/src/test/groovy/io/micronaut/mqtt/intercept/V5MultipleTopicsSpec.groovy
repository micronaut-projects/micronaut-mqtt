package io.micronaut.mqtt.intercept

import io.micronaut.context.annotation.Requires
import io.micronaut.mqtt.test.intercept.MultipleTopicsClient
import io.micronaut.mqtt.test.intercept.MultipleTopicsSpec
import io.micronaut.mqtt.annotation.v5.MqttPublisher

class V5MultipleTopicsSpec extends MultipleTopicsSpec {

    @Override
    Class<? extends MultipleTopicsClient> getClient() {
        return V3MultipleTopicsClient
    }

    @Requires(property = "spec.name", value = "V5MultipleTopicsSpec")
    @MqttPublisher
    static interface V3MultipleTopicsClient extends MultipleTopicsClient {}
}
