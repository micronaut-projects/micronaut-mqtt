package io.micronaut.mqtt.config

import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest(startApplication = false)
class MqttConfigurationSpec extends Specification {
    @Inject
    MqttConfiguration mqttConfiguration
    void "mqtt is enabled by default"() {
        expect:
        mqttConfiguration.enabled
    }
}
