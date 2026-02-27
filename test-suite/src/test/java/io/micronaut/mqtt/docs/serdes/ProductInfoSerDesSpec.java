package io.micronaut.mqtt.docs.serdes;

import io.micronaut.context.ApplicationContext;
import io.micronaut.mqtt.test.AbstractMQTTTest;
import org.junit.jupiter.api.Test;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

public class ProductInfoSerDesSpec implements AbstractMQTTTest {

    @Test
    void testUsingACustomSerDes() {
        ApplicationContext applicationContext = startContext();

// tag::producer[]
        ProductClient productClient = applicationContext.getBean(ProductClient.class);
        productClient.send(new ProductInfo("small", 10L, true));
        productClient.send(new ProductInfo("medium", 20L, true));
        productClient.send(new ProductInfo(null, 30L, false));
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
