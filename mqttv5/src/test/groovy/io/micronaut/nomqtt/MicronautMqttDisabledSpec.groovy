package io.micronaut.nomqtt

import io.micronaut.context.BeanContext
import io.micronaut.context.annotation.Property
import io.micronaut.core.util.StringUtils
import io.micronaut.mqtt.v5.intercept.MqttSubscriberAdvice
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@Property(name = "mqtt.enabled", value = StringUtils.FALSE)
@MicronautTest(startApplication = false)
class MicronautMqttDisabledSpec extends Specification {

    @Inject
    BeanContext beanContext

    void "setting mqtt.enabled to false disables MQTT at package-info.java"() {
        expect:
        !beanContext.containsBean(MqttSubscriberAdvice)
    }

}