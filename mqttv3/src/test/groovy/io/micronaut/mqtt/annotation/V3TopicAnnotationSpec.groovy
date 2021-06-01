package io.micronaut.mqtt.annotation

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.core.util.StringUtils
import io.micronaut.messaging.annotation.MessageMapping
import io.micronaut.mqtt.test.AbstractMQTTTest

class V3TopicAnnotationSpec extends AbstractMQTTTest {

    void "test that @Topic value aliases to @MessageMapping"() {
        given:
        ApplicationContext ctx = startContext("v3topicannotationtest": true)
        def definition = ctx.getBeanDefinition(MySubscriber)

        when:
        def method = definition.getRequiredMethod("receive", String)
        def annotationValue = method.getValue(MessageMapping, String[])

        then:
        annotationValue.isPresent()
        annotationValue.get().contains 'test/topic1'
    }


    @Requires(property = "v3topicannotationtest", value = StringUtils.TRUE)
    @MqttSubscriber
    static class MySubscriber {

        List<String> topics = []

        @Topic("test/topic1")
        void receive(String topic) {
            topics.add(topic)
        }
    }

}
