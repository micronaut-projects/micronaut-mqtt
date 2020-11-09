package io.micronaut.mqtt.docs.custom.type;

import io.micronaut.mqtt.AbstractRabbitMQTest;
import io.micronaut.context.ApplicationContext;
import org.junit.jupiter.api.Test;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

public class ProductInfoSpec extends AbstractRabbitMQTest {

    @Test
    void testUsingACustomTypeBinder() {
        ApplicationContext applicationContext = startContext();

// tag::producer[]
        ProductClient productClient = applicationContext.getBean(ProductClient.class);
        productClient.send("body".getBytes(), new ProductInfo("small", 10L, true));
        productClient.send("body2".getBytes(), new ProductInfo("medium", 20L, true));
        productClient.send("body3".getBytes(), new ProductInfo(null, 30L, true));
// end::producer[]

        ProductListener productListener = applicationContext.getBean(ProductListener.class);

        try {
            await().atMost(5, SECONDS).until(() ->
                    productListener.messages.size() == 3 &&
                    productListener.messages.stream().anyMatch(pi -> pi.getCount() == 10L) &&
                    productListener.messages.stream().anyMatch(pi -> pi.getCount() == 20L) &&
                    productListener.messages.stream().anyMatch(pi -> pi.getCount() == 30L)
            );
        } finally {
            applicationContext.close();
        }
    }
}
