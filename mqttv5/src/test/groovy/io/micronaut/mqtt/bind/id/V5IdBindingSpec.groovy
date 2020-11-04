package io.micronaut.mqtt.bind.id

import io.micronaut.context.annotation.Requires
import io.micronaut.mqtt.test.bind.id.IdBindingClient
import io.micronaut.mqtt.test.bind.id.IdBindingSpec
import io.micronaut.mqtt.v5.annotation.MqttClient

class V5IdBindingSpec extends IdBindingSpec {

    @Override
    Class<? extends IdBindingClient> getClient() {
        return MyClient
    }

    @Requires(property = "spec.name", value = "V5IdBindingSpec")
    @MqttClient
    static interface MyClient extends IdBindingClient {

    }
}
