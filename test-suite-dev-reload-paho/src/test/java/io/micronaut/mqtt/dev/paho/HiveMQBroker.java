/*
 * Copyright 2017-2026 original authors
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
package io.micronaut.mqtt.dev.paho;

import org.testcontainers.hivemq.HiveMQContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * One HiveMQ CE broker for the tests of the module.
 */
final class HiveMQBroker {

    private static HiveMQContainer container;

    private HiveMQBroker() {
    }

    static synchronized String serverUri() {
        if (container == null) {
            container = new HiveMQContainer(DockerImageName.parse("hivemq/hivemq-ce:2024.3"));
            container.start();
        }
        return "tcp://" + container.getHost() + ":" + container.getMqttPort();
    }

    static synchronized String host() {
        serverUri();
        return container.getHost();
    }

    static synchronized int port() {
        serverUri();
        return container.getMqttPort();
    }
}
