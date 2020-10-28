package io.micronaut.mqtt.test.intercept

import io.micronaut.mqtt.annotation.Topic

interface SimplePubSubClient {

    @Topic("test/simple")
    void publish(String body)

}
