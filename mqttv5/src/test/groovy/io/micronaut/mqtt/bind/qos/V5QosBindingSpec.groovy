package io.micronaut.mqtt.bind.qos

import io.micronaut.context.annotation.Requires
import io.micronaut.mqtt.test.bind.qos.QosBindingClient
import io.micronaut.mqtt.test.bind.qos.QosBindingSpec
import io.micronaut.mqtt.annotation.v5.MqttPublisher

class V5QosBindingSpec extends QosBindingSpec {

    @Override
    Class<? extends QosBindingClient> getClient() {
        return MyClient
    }

    @Requires(property = "spec.name", value = "V5QosBindingSpec")
    @MqttPublisher
    static interface MyClient extends QosBindingClient {

    }
}
