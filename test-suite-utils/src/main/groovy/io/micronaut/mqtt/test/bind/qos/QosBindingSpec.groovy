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
package io.micronaut.mqtt.test.bind.qos

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.core.util.StringUtils
import io.micronaut.mqtt.annotation.MqttSubscriber
import io.micronaut.mqtt.annotation.Qos
import io.micronaut.mqtt.annotation.Topic
import io.micronaut.mqtt.test.AbstractMQTTTest
import spock.util.concurrent.PollingConditions

abstract class QosBindingSpec extends AbstractMQTTTest {

    void "test qos binding"() {
        ApplicationContext ctx = startContext("qosbindingspec": true)
        def client = ctx.getBean(getClient())
        def sub = ctx.getBean(MySubscriber)
        def polling = new PollingConditions(timeout: 3)

        when:
        client.override()

        then:
        polling.eventually {
            assert sub.qos == 2
        }

        when:
        client.argument(1)

        then:
        polling.eventually {
            assert sub.qos == 1
        }

        when:
        client.classLevel()

        then:
        polling.eventually {
            assert sub.qos == 0
        }

        cleanup:
        ctx.close()
    }

    abstract Class<? extends QosBindingClient> getClient()

    @Requires(property = "qosbindingspec", value = StringUtils.TRUE)
    @MqttSubscriber
    static class MySubscriber {

        Integer qos = null

        @Topic(value = "test/qos", qos = 2)
        void get(@Qos Integer qos) {
            this.qos = qos
        }
    }
}
