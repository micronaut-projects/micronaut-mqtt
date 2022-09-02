package io.micronaut.mqtt

import io.micronaut.context.BeanContext
import io.micronaut.mqtt.exception.DefaultMqttSubscriberExceptionHandler
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest(startApplication = false)
class MqttEnabledSpec extends Specification {

    @Inject
    BeanContext beanContext

    void "by default mqtt.enabled is enabled"() {
        expect:
        beanContext.containsBean(DefaultMqttSubscriberExceptionHandler)
    }

}
