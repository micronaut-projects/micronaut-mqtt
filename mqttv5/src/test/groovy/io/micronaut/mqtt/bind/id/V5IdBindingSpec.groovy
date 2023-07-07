package io.micronaut.mqtt.bind.id

import io.micronaut.context.annotation.Requires
import io.micronaut.mqtt.test.MQTT5Test
import io.micronaut.mqtt.test.bind.id.IdBindingClient
import io.micronaut.mqtt.test.bind.id.IdBindingSpec
import io.micronaut.mqtt.annotation.v5.MqttPublisher

class V5IdBindingSpec extends IdBindingSpec implements MQTT5Test {

    @Override
    Class<? extends IdBindingClient> getClient() {
        return MyClient
    }

    @Requires(property = "spec.name", value = "V5IdBindingSpec")
    @MqttPublisher
    static interface MyClient extends IdBindingClient {

    }
}
