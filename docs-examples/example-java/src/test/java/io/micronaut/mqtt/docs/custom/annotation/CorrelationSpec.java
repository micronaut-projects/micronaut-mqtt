package io.micronaut.mqtt.docs.custom.annotation;

import io.micronaut.mqtt.AbstractRabbitMQTest;
import io.micronaut.context.ApplicationContext;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

public class CorrelationSpec extends AbstractRabbitMQTest {

    @Test
    void testUsingACustomAnnotationBinder() {
        ApplicationContext applicationContext = startContext();

// tag::producer[]
        ProductClient productClient = applicationContext.getBean(ProductClient.class);
        productClient.send("body".getBytes(), "a".getBytes());
        productClient.send("body2".getBytes(), "b".getBytes());
        productClient.send("body3".getBytes(), "c".getBytes());
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
