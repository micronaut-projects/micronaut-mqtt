package io.micronaut.mqtt.bind.retained

import io.micronaut.context.annotation.Requires
import io.micronaut.mqtt.test.bind.retained.RetainedBindingClient
import io.micronaut.mqtt.test.bind.retained.RetainedBindingSpec
import io.micronaut.mqtt.annotation.v3.MqttPublisher

class V3RetainedBindingSpec extends RetainedBindingSpec {

    @Override
    Class<? extends RetainedBindingClient> getClient() {
        return MyClient
    }

    @Requires(property = "spec.name", value = "V3RetainedBindingSpec")
    @MqttPublisher
    static interface MyClient extends RetainedBindingClient {

    }

}
