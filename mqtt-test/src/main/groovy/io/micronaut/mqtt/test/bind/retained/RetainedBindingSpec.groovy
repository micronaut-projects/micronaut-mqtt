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
package io.micronaut.mqtt.test.bind.retained

import io.micronaut.context.annotation.Requires
import io.micronaut.core.util.StringUtils
import io.micronaut.messaging.annotation.Body
import io.micronaut.mqtt.annotation.MqttSubscriber
import io.micronaut.mqtt.annotation.Topic
import io.micronaut.mqtt.test.AbstractMQTTTest
import spock.lang.Ignore
import spock.lang.Stepwise
import spock.util.concurrent.PollingConditions

@Stepwise
abstract class RetainedBindingSpec extends AbstractMQTTTest {

    void "test retained binding class value is used"() {
        def ctx = startContext() //the subscriber is disabled
        def client = ctx.getBean(getClient())
        //publish a message before there is a subscriber
        client.classLevel("classLevel")
        ctx.close()

        when:
        ctx = startContext("retainedbindingspec": true)
        def sub = ctx.getBean(MySubscriber)
        def polling = new PollingConditions(initialDelay: 3)

        then: //the subscriber didn't receive the message
        polling.eventually {
            assert sub.payload == null
        }

        cleanup:
        ctx.close()
    }

    void "test retained binding argument set to false"() {
        def ctx = startContext() //the subscriber is disabled
        def client = ctx.getBean(getClient())
        //publish a message before there is a subscriber
        client.argument(false, "argumentFalse")
        ctx.close()

        when:
        ctx = startContext("retainedbindingspec": true)
        def sub = ctx.getBean(MySubscriber)
        def polling = new PollingConditions(initialDelay: 3)

        then: //the subscriber didn't receive the message
        polling.eventually {
            assert sub.payload == null
        }

        cleanup:
        ctx.close()
    }

    void "test retained binding method overrides class"() {
        def ctx = startContext() //the subscriber is disabled
        def client = ctx.getBean(getClient())
        //publish a message before there is a subscriber
        client.override("overrides")
        ctx.close()

        when:
        ctx = startContext("retainedbindingspec": true)
        def sub = ctx.getBean(MySubscriber)
        def polling = new PollingConditions(timeout: 3)

        then: //the subscriber received the message
        polling.eventually {
            assert sub.payload == "overrides"
        }

        cleanup:
        ctx.close()
    }

    void "test retained binding argument set to true"() {
        def ctx = startContext() //the subscriber is disabled
        def client = ctx.getBean(getClient())
        //publish a message before there is a subscriber
        client.argument(true, "argumentTrue")
        ctx.close()

        when:
        def ctx2 = startContext("retainedbindingspec": true)
        def sub = ctx2.getBean(MySubscriber)
        def polling = new PollingConditions(timeout: 3)

        then: //the subscriber received the message
        polling.eventually {
            assert sub.payload == "argumentTrue"
        }

        cleanup:
        ctx.close()
        ctx2.close()
    }

    abstract Class<? extends RetainedBindingClient> getClient()

    @Requires(property = "retainedbindingspec", value = StringUtils.TRUE)
    @MqttSubscriber
    static class MySubscriber {

        String payload = null

        @Topic("test/retained")
        void get(@Body String payload) {
            this.payload = payload
        }
    }
}
