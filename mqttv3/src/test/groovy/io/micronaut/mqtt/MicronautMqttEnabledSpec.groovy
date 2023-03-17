package io.micronaut.mqtt

import io.micronaut.context.BeanContext
import io.micronaut.mqtt.v3.intercept.MqttSubscriberAdvice
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest(startApplication = false)
class MicronautMqttEnabledSpec extends Specification {

    @Inject
    BeanContext beanContext

    void "by default mqtt.enabled is enabled"() {
        expect:
        beanContext.containsBean(MqttSubscriberAdvice)
    }

}
