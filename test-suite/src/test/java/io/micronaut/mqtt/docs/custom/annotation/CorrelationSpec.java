package io.micronaut.mqtt.docs.custom.annotation;

import io.micronaut.mqtt.AbstractMQTTTest;
import io.micronaut.context.ApplicationContext;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

public class CorrelationSpec extends AbstractMQTTTest {

    @Test
    void testUsingACustomAnnotationBinder() {
        ApplicationContext applicationContext = startContext();

// tag::producer[]
        ProductClient productClient = applicationContext.getBean(ProductClient.class);
        productClient.send("a".getBytes());
        productClient.send("b".getBytes());
        productClient.send("c".getBytes());
// end::producer[]

        ProductListener productListener = applicationContext.getBean(ProductListener.class);

        try {
            Set<String> expectedMessages = new HashSet<>();
            expectedMessages.add("a");
            expectedMessages.add("b");
            expectedMessages.add("c");
            await().atMost(5, SECONDS).until(() ->
                    productListener.messages.size() == 3 &&
                    productListener.messages.equals(expectedMessages)
            );
        } finally {
            applicationContext.close();
        }
    }
}
