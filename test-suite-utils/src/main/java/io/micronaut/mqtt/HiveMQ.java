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
package io.micronaut.mqtt;

import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.hivemq.HiveMQContainer;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

public class HiveMQ {

    private static final String IMAGE_NAME = "hivemq/hivemq-ce:2024.3";
    private static HiveMQContainer container;

    public static Map<String, String> getProperties() {
        if (container == null) {
            container = new HiveMQContainer(DockerImageName.parse(IMAGE_NAME))
                .waitingFor(Wait.forListeningPort());
            container.start();
            do {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            } while(!container.isRunning());
            return getProperties(container);
        } else {
            return getProperties(container);
        }
    }

    private static Map<String, String> getProperties(HiveMQContainer container) {
        String serverUri = "tcp://" + container.getHost() + ":" + container.getMqttPort();
        return Map.of(
            "mqtt.client.server-uri", serverUri,
            "mqtt.client.client-id", "micronaut"
        );
    }

}
