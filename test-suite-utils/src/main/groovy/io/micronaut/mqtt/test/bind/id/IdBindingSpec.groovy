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
package io.micronaut.mqtt.test.bind.id

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.core.util.StringUtils
import io.micronaut.mqtt.annotation.Id
import io.micronaut.mqtt.annotation.MqttSubscriber
import io.micronaut.mqtt.annotation.Topic
import io.micronaut.mqtt.test.AbstractMQTTTest
import spock.util.concurrent.PollingConditions

abstract class IdBindingSpec extends AbstractMQTTTest {

    void "test id binding"() {
        ApplicationContext ctx = startContext("idbindingspec": true)
        def client = ctx.getBean(getClient())
        def sub = ctx.getBean(MySubscriber)
        def polling = new PollingConditions(timeout: 3)

        when:
        client.send()
        client.send2()

        then:
        polling.eventually {
            assert sub.id1 != null
            assert sub.id2 != null
        }
    }

    abstract Class<? extends IdBindingClient> getClient()

    @Requires(property = "idbindingspec", value = StringUtils.TRUE)
    @MqttSubscriber
    static class MySubscriber {

        Integer id1 = null
        Integer id2 = null

        @Topic("test/id/1")
        void get1(@Id Integer id) {
            this.id1 = id
        }

        @Topic("test/id/2")
        void get2(Integer id) {
            this.id2 = id
        }
    }
}
