package io.micronaut.mqtt.ssl

import io.micronaut.context.ApplicationContext
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class SslAuthenticationSpec extends Specification implements io.micronaut.mqtt.test.AbstractMQTTTest {

    // Provide MQTT version for this SSL test
    int getMqttVersion() {
        return 3
    }

    @Override
    ApplicationContext startContext(Map<String, Object> additionalConfig = [:]) {
        // Use SSL-specific connection settings
        Map<String, Object> config = new HashMap<>()
        config.put("mqtt.client.server-uri", "ssl://localhost:${SslContainer.mqttContainer.getMappedPort(8883)}")
        config.put("mqtt.client.client-id", UUID.randomUUID().toString())
        config.put("mqtt.client.mqtt-version", getMqttVersion())
        config.put("mqtt.client.ssl.certificate-authority", "classpath:certs/ca-cert.pem")
        config.put("mqtt.client.ssl.certificate", "classpath:certs/client-cert.pem")
        config.put("mqtt.client.ssl.private-key", "classpath:certs/client-key.pem")
        config.put("mqtt.client.ssl.password", "test")
        config.put("spec.name", getClass().getSimpleName())
        config.putAll(additionalConfig)

        return ApplicationContext.run(config, "test")
    }

    static class SslContainer {
        static final GenericContainer<?> mqttContainer =
                new GenericContainer<>(DockerImageName.parse("eclipse-mosquitto:1.6.12"))
                        .withExposedPorts(8883)
                        .waitingFor(Wait.forListeningPort())
                        .withClasspathResourceMapping("mosquitto.conf",
                                "/mosquitto/config/mosquitto.conf",
                                BindMode.READ_ONLY)
                        .withClasspathResourceMapping("certs/",
                                "/mosquitto/config/certs/",
                                BindMode.READ_ONLY)

        static {
            mqttContainer.start()
        }
    }

    void "test product client and listener"() {
        ApplicationContext applicationContext = startContext()
        PollingConditions conditions = new PollingConditions(timeout: 5)

        when:
        def productClient = applicationContext.getBean(ProductClient)
        productClient.send("quickstart".bytes)

        ProductListener productListener = applicationContext.getBean(ProductListener)

        then:
        conditions.eventually {
            productListener.messageLengths.size() == 1
            productListener.messageLengths[0] == "quickstart"
        }

        cleanup:
        // Finding that the context is closing the channel before ack is sent
        Thread.sleep(200)
        applicationContext.close()
    }
}
