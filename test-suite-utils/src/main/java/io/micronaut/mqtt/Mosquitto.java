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

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

public class Mosquitto {
    private static final GenericContainer<?> INSTANCE = new GenericContainer<>(DockerImageName.parse("eclipse-mosquitto:1.6.12"))
            .withExposedPorts(1883)
            .waitingFor(Wait.forListeningPort());

    static {
        INSTANCE.start();
    }

    public static Map<String, String> getProperties() {
        return Map.of(
                "mqtt.client.server-uri", "tcp://" + INSTANCE.getHost() + ":" + INSTANCE.getMappedPort(1883)
        );
    }
}
