package example.micronaut;

import io.micronaut.mqtt.annotation.Topic;
import io.micronaut.mqtt.annotation.v5.MqttPublisher;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest
class SubscriptionTest {

    @Inject
    SmellClient client;

    @Inject
    SmellListener listener;

    @Test
    void checkSubscriptionsAreReceived() {
        client.publishLivingroomSmell(new Odour("cheesy"));

        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() -> assertEquals("cheesy", listener.getSmell()));
    }

    @MqttPublisher
    interface SmellClient {

        @Topic("house/livingroom/smell")
        void publishLivingroomSmell(Odour data);
    }
}
