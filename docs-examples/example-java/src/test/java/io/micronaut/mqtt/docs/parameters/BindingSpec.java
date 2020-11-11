package io.micronaut.mqtt.docs.parameters;

import io.micronaut.mqtt.AbstractMQTTTest;
import io.micronaut.context.ApplicationContext;
import org.junit.jupiter.api.Test;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

public class BindingSpec extends AbstractMQTTTest {

    @Test
    void testDynamicBinding() {
        ApplicationContext applicationContext = startContext();

// tag::producer[]
        ProductClient productClient = applicationContext.getBean(ProductClient.class);
        productClient.send("message body".getBytes());
        productClient.send("product/a", "message body2".getBytes());
        productClient.send("product/b", "message body2".getBytes());
// end::producer[]

        ProductListener productListener = applicationContext.getBean(ProductListener.class);

        try {
            await().atMost(5, SECONDS).until(() ->
                    productListener.messageLengths.size() == 1 &&
                    productListener.messageLengths.contains(12) &&
                    productListener.topics.size() == 2 &&
                    productListener.topics.get(0).equals("product/a") &&
                    productListener.topics.get(1).equals("product/b")
            );
        } finally {
            applicationContext.close();
        }
    }
}
