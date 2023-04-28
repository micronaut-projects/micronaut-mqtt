package io.micronaut.mqtt.bind.id

import io.micronaut.context.annotation.Requires
import io.micronaut.mqtt.test.bind.id.IdBindingClient
import io.micronaut.mqtt.test.bind.id.IdBindingSpec
import io.micronaut.mqtt.annotation.v3.MqttPublisher

class V3IdBindingSpec extends IdBindingSpec {

    @Override
    Class<? extends IdBindingClient> getClient() {
        return MyClient
    }

    @Requires(property = "spec.name", value = "V3IdBindingSpec")
    @MqttPublisher
    static interface MyClient extends IdBindingClient {

    }
}
