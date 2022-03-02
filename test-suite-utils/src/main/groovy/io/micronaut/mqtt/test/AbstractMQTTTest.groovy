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
package io.micronaut.mqtt.test

import io.micronaut.context.ApplicationContext
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy
import org.testcontainers.utility.DockerImageName
import spock.lang.Specification

abstract class AbstractMQTTTest extends Specification {

    static GenericContainer mqttContainer =
            new GenericContainer(DockerImageName.parse("eclipse-mosquitto:1.6.12"))
                    .withExposedPorts(1883)
                    .waitingFor(new LogMessageWaitStrategy().withRegEx("(?s).*mosquitto version 1.6.12 running.*"))
                    .withClasspathResourceMapping("mosquitto.conf",
                            "/mosquitto/config/mosquitto.conf",
                            BindMode.READ_ONLY)


    static {
        mqttContainer.start()
    }

    protected ApplicationContext startContext(Map additionalConfig = [:]) {
        ApplicationContext.run(
                ["mqtt.client.server-uri": "tcp://localhost:${mqttContainer.getMappedPort(1883)}",
                 "mqtt.client.client-id": UUID.randomUUID().toString(),
                 "endpoints.health.mqtt.client.enabled": true,
                 "spec.name": getClass().simpleName] << additionalConfig, "test")
    }
}
