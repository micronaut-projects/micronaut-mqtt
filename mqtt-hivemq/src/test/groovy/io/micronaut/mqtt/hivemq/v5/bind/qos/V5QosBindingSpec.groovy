package io.micronaut.mqtt.hivemq.v5.bind.qos

import io.micronaut.context.annotation.Requires
import io.micronaut.mqtt.annotation.v5.MqttPublisher
import io.micronaut.mqtt.test.MQTT5Test
import io.micronaut.mqtt.test.bind.qos.QosBindingClient
import io.micronaut.mqtt.test.bind.qos.QosBindingSpec

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

class V5QosBindingSpec extends QosBindingSpec implements MQTT5Test {

    @Override
    Class<? extends QosBindingClient> getClient() {
        return MyClient
    }

    @Requires(property = "spec.name", value = "V5QosBindingSpec")
    @MqttPublisher
    static interface MyClient extends QosBindingClient {

    }
}
