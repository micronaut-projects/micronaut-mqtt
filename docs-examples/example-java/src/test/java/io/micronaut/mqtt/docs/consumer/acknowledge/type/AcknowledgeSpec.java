package io.micronaut.mqtt.docs.consumer.acknowledge.type;

import io.micronaut.mqtt.AbstractRabbitMQTest;
import io.micronaut.context.ApplicationContext;
import org.junit.jupiter.api.Test;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

public class AcknowledgeSpec extends AbstractRabbitMQTest {

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
