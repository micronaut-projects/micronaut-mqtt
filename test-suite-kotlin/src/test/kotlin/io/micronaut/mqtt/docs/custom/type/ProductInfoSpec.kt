package io.micronaut.mqtt.docs.custom.type

import io.micronaut.mqtt.AbstractMQTTTest
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

class ProductInfoSpec : AbstractMQTTTest() {

    @Test
    fun testCustomTypeBinder() {
        val applicationContext = startContext()

        // tag::producer[]
        val productClient = applicationContext.getBean(ProductClient::class.java)
        productClient.send(ProductInfo("small", 10L, true))
        productClient.send(ProductInfo("medium", 20L, false))
        productClient.send(ProductInfo(null, 30L, true))
        // end::producer[]

        val productListener = applicationContext.getBean(ProductListener::class.java)

        try {
            await().atMost(5, TimeUnit.SECONDS).until {
                productListener.messages.size == 3 &&
                        productListener.messages.any { p -> p.size == "small" && p.count == 10L && p.sealed } &&
                        productListener.messages.any { p -> p.size == "medium" && p.count == 20L && !p.sealed } &&
                        productListener.messages.any { p -> p.size == null && p.count == 30L && p.sealed }
            }
        } finally {
            applicationContext.close()
        }
    }
}
