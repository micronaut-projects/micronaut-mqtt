package io.micronaut.mqtt

import io.micronaut.context.ApplicationContext
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy
import java.util.*

abstract class AbstractMQTTTest {

    companion object {
        val mqttContainer = KGenericContainer("eclipse-mosquitto:1.6.12")
                .withExposedPorts(1883)
                .waitingFor(LogMessageWaitStrategy().withRegEx("(?s).*mosquitto version 1.6.12 running.*"))
                .withClasspathResourceMapping("mosquitto.conf",
                        "/mosquitto/config/mosquitto.conf",
                        BindMode.READ_ONLY)!!

        init {
            mqttContainer.start()
        }
    }

    fun startContext(): ApplicationContext {
        return ApplicationContext.run(getConfiguration(), "test")
    }

    open fun getConfiguration(): MutableMap<String, Any> {
        return mutableMapOf(
                "mqtt.client.server-uri" to "tcp://localhost:${mqttContainer.getMappedPort(1883)}",
                "mqtt.client.client-id" to UUID.randomUUID().toString(),
                "spec.name" to javaClass.simpleName)
    }
}
