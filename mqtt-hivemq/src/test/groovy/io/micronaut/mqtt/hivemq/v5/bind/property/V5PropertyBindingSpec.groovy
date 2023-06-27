package io.micronaut.mqtt.hivemq.v5.bind.property

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.core.annotation.Nullable
import io.micronaut.mqtt.annotation.MqttSubscriber
import io.micronaut.mqtt.annotation.Topic
import io.micronaut.mqtt.annotation.v5.MqttProperty
import io.micronaut.mqtt.annotation.v5.MqttPublisher
import io.micronaut.mqtt.test.AbstractMQTTTest
import io.micronaut.mqtt.test.MQTT5Test
import spock.util.concurrent.PollingConditions

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

class V5PropertyBindingSpec extends AbstractMQTTTest implements MQTT5Test {

    void "test property binding"() {
        ApplicationContext ctx = startContext()
        def client = ctx.getBean(MyClient)
        def sub = ctx.getBean(MySubscriber)
        def polling = new PollingConditions(timeout: 3)

        when:
        client.send()

        then:
        polling.eventually {
            assert sub.contentType == "application/json"
            assert sub.nullable == null
        }

        when:
        client.sendArg("application/xml")

        then:
        polling.eventually {
            assert sub.contentType == "application/xml"
            assert sub.nullable == null
        }

        when:
        client.sendCustom("this is a user property")

        then:
        polling.eventually {
            assert sub.customProp == "this is a user property"
        }

        cleanup:
        ctx.close()
    }

    @Requires(property = "spec.name", value = "V5PropertyBindingSpec")
    @Topic("test/property")
    @MqttPublisher
    static interface MyClient {

        @MqttProperty(name = "contentType", value = "application/json")
        void send()

        void sendArg(@MqttProperty(name = "contentType") String contentType)

        @Topic("test/property/custom")
        void sendCustom(@MqttProperty String customUserProperty)
    }

    @Requires(property = "spec.name", value = "V5PropertyBindingSpec")
    @MqttSubscriber
    static class MySubscriber {

        String contentType
        String nullable
        String customProp

        @Topic("test/property")
        void get(@MqttProperty String contentType, @Nullable @MqttProperty String nullable) {
            this.contentType = contentType
            this.nullable = nullable
        }

        @Topic("test/property/custom")
        void getCustom(@MqttProperty("customUserProperty") String cup) {
            this.customProp = cup
        }
    }
}
