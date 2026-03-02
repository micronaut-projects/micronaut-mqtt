package io.micronaut.mqtt.docs.consumer.acknowledge.type;

import io.micronaut.context.ApplicationContext;
import io.micronaut.mqtt.test.AbstractMQTTTest;
import org.junit.jupiter.api.Test;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

public class AcknowledgeSpec implements AbstractMQTTTest {

    @Test
    void testAckingWithAcknowledgement() {
        ApplicationContext applicationContext = startContext();

// tag::producer[]
ProductClient productClient = applicationContext.getBean(ProductClient.class);
productClient.send("message body".getBytes());
productClient.send("message body".getBytes());
productClient.send("message body".getBytes());
productClient.send("message body".getBytes());
// end::producer[]

        ProductListener productListener = applicationContext.getBean(ProductListener.class);

        try {
            await().atMost(5, SECONDS).until(() ->
                    productListener.messageCount.get() == 4
            );
        } finally {
            applicationContext.close();
        }
    }
}
