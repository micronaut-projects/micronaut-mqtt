/*
 * Copyright 2017-2020 original authors
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
package io.micronaut.mqtt.test.bind.topic

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.core.util.StringUtils
import io.micronaut.mqtt.annotation.MqttSubscriber
import io.micronaut.mqtt.annotation.Topic
import io.micronaut.mqtt.test.AbstractMQTTTest
import spock.util.concurrent.PollingConditions

abstract class TopicBindingSpec extends AbstractMQTTTest {

    void "test topic binding"() {
        ApplicationContext ctx = startContext("topicbindingspec": true)
        def client = ctx.getBean(getClient())
        def sub = ctx.getBean(MySubscriber)
        def polling = new PollingConditions(timeout: 3)

        when:
        client.override()

        then:
        polling.eventually {
            assert sub.topic == "test/topic/override"
        }

        when:
        client.argument("test/topic/arg")

        then:
        polling.eventually {
            assert sub.topic == "test/topic/arg"
        }

        when:
        client.classLevelTopic()

        then:
        polling.eventually {
            assert sub.topic == "test/topic/classlevel"
        }

        cleanup:
        ctx.close()
    }

    abstract Class<? extends TopicBindingClient> getClient()

    @Requires(property = "topicbindingspec", value = StringUtils.TRUE)
    @MqttSubscriber
    static class MySubscriber {

        String topic

        @Topic("test/topic/#")
        void get(@Topic String topic) {
            this.topic = topic
        }
    }
}
