package io.micronaut.mqtt.docs;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextConfigurer;
import io.micronaut.context.annotation.ContextConfigurer;
import io.micronaut.context.env.Environment;
import io.micronaut.context.env.PropertySource;
import io.micronaut.mqtt.Mosquitto;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Supplies the {@code mqtt.client.*} configuration of the shared Mosquitto test container to the
 * Python tests run with the {@code mqtt} environment, like {@code AbstractMQTTTest.startContext()}
 * does for the Java, Kotlin and Groovy suites.
 * <p>
 * The configurer is written in Java because Micronaut Test calls {@code TestPropertyProvider} before
 * the application context, and with it the GraalPy runtime, exists, so a Python test class cannot
 * supply the container properties. It uses the {@link #configure(ApplicationContext)} callback because
 * the {@link io.micronaut.context.ApplicationContextBuilder} is configured before {@code @MicronautTest}
 * selects the environments, so the {@code mqtt} environment can only be checked on the built context.
 */
@ContextConfigurer
public class MqttTestConfigurer implements ApplicationContextConfigurer {

    public static final String MQTT_ENVIRONMENT = "mqtt";

    @Override
    public void configure(ApplicationContext applicationContext) {
        Environment environment = applicationContext.getEnvironment();
        if (environment.getActiveNames().contains(MQTT_ENVIRONMENT)) {
            Map<String, Object> config = new HashMap<>(Mosquitto.getProperties());
            config.put("mqtt.client.client-id", UUID.randomUUID().toString());
            environment.addPropertySource(PropertySource.of(MQTT_ENVIRONMENT, config));
        }
    }
}
