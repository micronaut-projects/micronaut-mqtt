package io.micronaut.mqtt

import io.kotest.core.spec.style.BehaviorSpec
import io.micronaut.context.ApplicationContext
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy

abstract class AbstractMqttKotest(body: BehaviorSpec.() -> Unit = {}): BehaviorSpec(body) {

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

        fun startContext(specName: String): ApplicationContext {
            return ApplicationContext.run(getDefaultConfig(specName), "test")
        }

        fun startContext(configuration: Map<String, Any>): ApplicationContext {
            return ApplicationContext.run(configuration, "test")
        }

        fun getDefaultConfig(specName: String): MutableMap<String, Any> {
            return mutableMapOf(
                    "mqtt.client.server-uri" to "tcp://localhost:${mqttContainer.getMappedPort(1883)}",
                    "spec.name" to specName)
        }

    }

}
