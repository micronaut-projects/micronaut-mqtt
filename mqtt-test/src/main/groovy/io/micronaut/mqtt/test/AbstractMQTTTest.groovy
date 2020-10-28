package io.micronaut.mqtt.test;

import io.micronaut.context.ApplicationContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import spock.lang.Specification;

import java.util.Map;

abstract class AbstractMQTTTest extends Specification {

    static GenericContainer mqttContainer =
            new GenericContainer("eclipse-mosquitto:1.6.12")
                    .withExposedPorts(1883)
                    .waitingFor(new LogMessageWaitStrategy().withRegEx("(?s).*mosquitto version 1.6.12 running.*"))
                    .withClasspathResourceMapping("mosquitto.conf",
                            "/mosquitto/config/mosquitto.conf",
                            BindMode.READ_ONLY)


    static {
        mqttContainer.start()
        //def result = mqttContainer.execInContainer("cat", "/mosquitto/config/mosquitto.conf")
        //System.out.println(result.getStdout())
    }

    protected ApplicationContext startContext(Map additionalConfig = [:]) {
        ApplicationContext.run(
                ["mqtt.client.server-uri": "tcp://localhost:${mqttContainer.getMappedPort(1883)}",
                 "mqtt.client.client-id": UUID.randomUUID().toString(),
                 "spec.name": getClass().simpleName] << additionalConfig, "test")
    }
}
