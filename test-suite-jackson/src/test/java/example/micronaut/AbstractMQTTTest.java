package example.micronaut;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;
import java.util.UUID;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class AbstractMQTTTest implements TestPropertyProvider {

    protected static final GenericContainer<?> mqttContainer = new GenericContainer<>(DockerImageName.parse("eclipse-mosquitto:1.6.12"))
        .withExposedPorts(1883)
        .waitingFor(Wait.forListeningPort())
        .withClasspathResourceMapping("mosquitto.conf",
            "/mosquitto/config/mosquitto.conf", BindMode.READ_ONLY);

    static {
        mqttContainer.start();
    }

    @Override
    public Map<String, String> getProperties() {
        String host = mqttContainer.getHost();
        Integer port = mqttContainer.getMappedPort(1883);

        return Map.of(
            "mqtt.client.server-uri", "tcp://" + host + ":" + port,
            "mqtt.client.client-id", "test-client-" + UUID.randomUUID(),
            "mqtt.client.ssl.enabled", "false",
            "spec.name", this.getClass().getSimpleName(),
            "micronaut.executors.default.name", "AbstractMQTTTest"
        );
    }
}
