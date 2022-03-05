/*
 * Copyright 2017-2022 original authors
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
package io.micronaut.mqtt.test.intercept

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.core.util.StringUtils
import io.micronaut.mqtt.annotation.MqttSubscriber
import io.micronaut.mqtt.annotation.Topic
import io.micronaut.mqtt.test.AbstractMQTTTest
import spock.util.concurrent.PollingConditions

abstract class MultipleTopicsSpec extends AbstractMQTTTest {

    void "test subscribing to multiple topics"() {
        ApplicationContext ctx = startContext("multipletopicstest": true)
        def client = ctx.getBean(getClient())
        def subscriber = ctx.getBean(MySubscriber)
        def subscriber2 = ctx.getBean(MySubscriber2)
        def polling = new PollingConditions(timeout: 3)

        when:
        client.override()

        then:
        polling.eventually {
            assert subscriber.topics.size() == 1
            assert subscriber.topics[0] == "test/topic1"
        }

        when:
        client.send("test/topic2")

        then:
        polling.eventually {
            assert subscriber.topics.size() == 2
            assert subscriber.topics[1] == "test/topic2"
        }

        when:
        client.send("test/topic/classlevel/1")

        then:
        polling.eventually {
            assert subscriber2.topics.size() == 1
            assert subscriber2.topics[0] == "test/topic/classlevel/1"
        }

        when:
        client.send("test/topic/classlevel/2")

        then:
        polling.eventually {
            assert subscriber2.topics.size() == 2
            assert subscriber2.topics[1] == "test/topic/classlevel/2"
        }

        cleanup:
        ctx.close()
    }

    abstract Class<? extends MultipleTopicsClient> getClient()

    @Requires(property = "multipletopicstest", value = StringUtils.TRUE)
    @MqttSubscriber
    static class MySubscriber {

        List<String> topics = []

        @Topic("test/topic1")
        @Topic("test/topic2")
        void get(String topic) {
            topics.add(topic)
        }
    }

    @Requires(property = "multipletopicstest", value = StringUtils.TRUE)
    @Topic("test/topic/classlevel/1")
    @MqttSubscriber
    static class MySubscriber2 {

        List<String> topics = []

        @Topic("test/topic/classlevel/2")
        void get(String topic) {
            topics.add(topic)
        }
    }
}
