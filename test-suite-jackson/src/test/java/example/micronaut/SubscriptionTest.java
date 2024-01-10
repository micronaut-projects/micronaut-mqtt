package example.micronaut;

import io.micronaut.context.ApplicationContext;
import io.micronaut.mqtt.annotation.Topic;
import io.micronaut.mqtt.annotation.v5.MqttPublisher;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SubscriptionTest extends AbstractMQTTTest {

    @Test
    void checkSubscriptionsAreReceived() {
        try(ApplicationContext applicationContext = startContext()) {
            SmellClient client = applicationContext.getBean(SmellClient.class);
            SmellListener listener = applicationContext.getBean(SmellListener.class);

            client.publishLivingroomSmell(new Odour("cheesy"));

            await()
                .atMost(5, TimeUnit.SECONDS)
                .untilAsserted(() -> assertEquals("cheesy", listener.getSmell()));
        }
    }

    @MqttPublisher
    interface SmellClient {

        @Topic("house/livingroom/smell")
        void publishLivingroomSmell(Odour data);
    }
}
