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
package io.micronaut.mqtt.test;

import io.micronaut.context.ApplicationContext;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public interface AbstractMQTTTest {

    class MQTTContainer {
        static final GenericContainer<?> mqttContainer = createContainer();

        private static GenericContainer<?> createContainer() {
            GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse("eclipse-mosquitto:1.6.12"))
                    .withExposedPorts(1883)
                    .waitingFor(Wait.forListeningPort());

            // Only add classpath resource mapping if not running in native image
            if (!isNativeImage()) {
                container.withClasspathResourceMapping("mosquitto.conf",
                        "/mosquitto/config/mosquitto.conf",
                        BindMode.READ_ONLY);
            }

            return container;
        }

        private static boolean isNativeImage() {
            return "runtime".equals(System.getProperty("org.graalvm.nativeimage.imagecode"));
        }

        static {
            mqttContainer.start();
        }
    }

    // getMqttVersion() should be provided by implementing MQTT3Test or MQTT5Test interfaces

    default ApplicationContext startContext() {
        return startContext(new HashMap<>());
    }

    default ApplicationContext startContext(Map<String, Object> additionalConfig) {
        Map<String, Object> config = new HashMap<>();
        config.put("mqtt.client.server-uri", "tcp://localhost:" + MQTTContainer.mqttContainer.getMappedPort(1883));
        config.put("mqtt.client.client-id", UUID.randomUUID().toString());

        // Get MQTT version from the implementing interface or method
        int mqttVersion = -1;
        if (this instanceof MQTT3Test) {
            mqttVersion = ((MQTT3Test) this).getMqttVersion();
        } else if (this instanceof MQTT5Test) {
            mqttVersion = ((MQTT5Test) this).getMqttVersion();
        }
        if (mqttVersion != -1) {
            config.put("mqtt.client.mqtt-version", mqttVersion);
        }
        config.put("endpoints.health.mqtt.client.enabled", true);
        config.put("spec.name", getClass().getSimpleName());
        config.putAll(additionalConfig);

        return ApplicationContext.run(config, "test");
    }
}
