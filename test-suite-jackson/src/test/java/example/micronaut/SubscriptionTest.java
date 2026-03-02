package example.micronaut;

import io.micronaut.mqtt.annotation.Topic;
import io.micronaut.mqtt.annotation.v5.MqttPublisher;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SubscriptionTest extends AbstractMQTTTest {

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
