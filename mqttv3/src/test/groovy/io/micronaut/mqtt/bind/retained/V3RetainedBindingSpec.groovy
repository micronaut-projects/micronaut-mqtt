package io.micronaut.mqtt.bind.retained

import io.micronaut.context.annotation.Requires
import io.micronaut.core.util.StringUtils
import io.micronaut.mqtt.annotation.MqttSubscriber
import io.micronaut.mqtt.annotation.Retained
import io.micronaut.mqtt.annotation.Topic
import io.micronaut.mqtt.test.AbstractMQTTTest
import io.micronaut.mqtt.test.bind.retained.RetainedBindingClient
import io.micronaut.mqtt.test.bind.retained.RetainedBindingSpec
import io.micronaut.mqtt.v3.annotation.MqttClient
import spock.util.concurrent.PollingConditions

class V3RetainedBindingSpec extends RetainedBindingSpec {

    @Override
    Class<? extends RetainedBindingClient> getClient() {
        return MyClient
    }

    @Requires(property = "spec.name", value = "V3RetainedBindingSpec")
    @MqttClient
    static interface MyClient extends RetainedBindingClient {

    }

}
