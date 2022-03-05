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

abstract class SimplePubSubSpec extends AbstractMQTTTest {

    void "test simple publishing and subscribing"() {
        ApplicationContext ctx = startContext("simplepubsubtest": true)
        def client = ctx.getBean(getClient())
        def subscriber = ctx.getBean(MySubscriber)
        def polling = new PollingConditions(timeout: 3)

        when:
        client.publish("test body")

        then:
        polling.eventually {
            assert subscriber.payload == "test body"
        }

        cleanup:
        ctx.close()
    }

    abstract Class<? extends SimplePubSubClient> getClient()

    @Requires(property = "simplepubsubtest", value = StringUtils.TRUE)
    @MqttSubscriber
    static class MySubscriber {

        String payload

        @Topic("test/simple")
        void get(String payload) {
            this.payload = payload
        }
    }
}
