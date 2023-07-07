package io.micronaut.mqtt.hivemq.v3.annotation

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.core.util.StringUtils
import io.micronaut.messaging.annotation.MessageMapping
import io.micronaut.mqtt.annotation.MqttSubscriber
import io.micronaut.mqtt.annotation.Topic
import io.micronaut.mqtt.test.AbstractMQTTTest
import io.micronaut.mqtt.test.MQTT3Test

/*
 * Copyright 2017-2023 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

class V3TopicAnnotationSpec extends AbstractMQTTTest implements MQTT3Test {

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
