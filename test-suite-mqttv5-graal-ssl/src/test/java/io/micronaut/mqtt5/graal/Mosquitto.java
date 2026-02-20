package io.micronaut.mqtt5.graal;

import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;

public class Mosquitto {

    private static final String IMAGE_NAME = "eclipse-mosquitto:1.6.12";
    private static GenericContainer<?> container;

    public static Map<String, String> getProperties() {
        if (container == null) {
            container = new GenericContainer<>(DockerImageName.parse(IMAGE_NAME))
                .withExposedPorts(8883)
                .waitingFor(Wait.forListeningPort())
                .withFileSystemBind("mosquitto/mosquitto.conf",
                    "/mosquitto/config/mosquitto.conf", BindMode.READ_ONLY)
                .withFileSystemBind("mosquitto/certs/",
                    "/mosquitto/config/certs/", BindMode.READ_ONLY);

            container.start();
        }
        return getProperties(container);
    }

    private static Map<String, String> getProperties(GenericContainer<?> container) {
        String host = container.getHost();
        Integer mappedPort = container.getMappedPort(8883);
        String serverUri = String.format("ssl://%s:%d", host, mappedPort);

        return Map.of(
            "mqtt.client.server-uri", serverUri,
            "mqtt.client.client-id", "micronaut"
        );
    }
}
