package io.micronaut.mqtt.bind.retained

import io.micronaut.context.annotation.Requires
import io.micronaut.mqtt.test.bind.retained.RetainedBindingClient
import io.micronaut.mqtt.test.bind.retained.RetainedBindingSpec
import io.micronaut.mqtt.v5.annotation.MqttPublisher

class V5RetainedBindingSpec extends RetainedBindingSpec {

    @Override
    Class<? extends RetainedBindingClient> getClient() {
        return MyClient
    }

    @Requires(property = "spec.name", value = "V5RetainedBindingSpec")
    @MqttPublisher
    static interface MyClient extends RetainedBindingClient {

    }

}
