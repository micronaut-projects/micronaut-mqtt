package example.micronaut;

import io.micronaut.context.ApplicationContext;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy;
import org.testcontainers.utility.DockerImageName;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public abstract class AbstractMQTTTest {

    protected static GenericContainer mqttContainer = new GenericContainer(DockerImageName.parse("eclipse-mosquitto:1.6.12"))
            .withExposedPorts(1883)
            .waitingFor(new LogMessageWaitStrategy().withRegEx("(?s).*mosquitto version 1.6.12 running.*"))
            .withClasspathResourceMapping("mosquitto.conf",
                    "/mosquitto/config/mosquitto.conf",
                    BindMode.READ_ONLY);

    static {
        mqttContainer.start();
    }

    protected ApplicationContext startContext() {
        return ApplicationContext.run(getConfiguration(), "test");
    }

    protected Map<String, Object> getConfiguration() {
        Map<String, Object> config = new HashMap<>();
        config.put("mqtt.client.server-uri", "tcp://localhost:" + mqttContainer.getMappedPort(1883));
        config.put("mqtt.client.client-id", UUID.randomUUID().toString());
        config.put("spec.name", this.getClass().getSimpleName());
        return config;
    }
}
