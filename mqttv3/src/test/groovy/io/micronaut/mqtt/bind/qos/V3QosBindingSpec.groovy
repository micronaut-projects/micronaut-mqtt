package io.micronaut.mqtt.bind.qos

import io.micronaut.context.annotation.Requires
import io.micronaut.mqtt.test.bind.qos.QosBindingClient
import io.micronaut.mqtt.test.bind.qos.QosBindingSpec
import io.micronaut.mqtt.v3.annotation.MqttClient

class V3QosBindingSpec extends QosBindingSpec {

    @Override
    Class<? extends QosBindingClient> getClient() {
        return MyClient
    }

    @Requires(property = "spec.name", value = "V3QosBindingSpec")
    @MqttClient
    static interface MyClient extends QosBindingClient {

    }
}
