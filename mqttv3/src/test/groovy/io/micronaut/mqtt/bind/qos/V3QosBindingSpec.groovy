package io.micronaut.mqtt.bind.qos

import io.micronaut.context.annotation.Requires
import io.micronaut.mqtt.test.bind.qos.QosBindingClient
import io.micronaut.mqtt.test.bind.qos.QosBindingSpec
import io.micronaut.mqtt.v3.annotation.MqttPublisher

class V3QosBindingSpec extends QosBindingSpec {

    @Override
    Class<? extends QosBindingClient> getClient() {
        return MyClient
    }

    @Requires(property = "spec.name", value = "V3QosBindingSpec")
    @MqttPublisher
    static interface MyClient extends QosBindingClient {

    }
}
