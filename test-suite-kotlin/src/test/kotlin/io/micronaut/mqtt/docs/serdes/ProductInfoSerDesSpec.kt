package io.micronaut.mqtt.docs.serdes

import io.micronaut.mqtt.AbstractMQTTTest
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

class ProductInfoSerDesSpec : AbstractMQTTTest() {

    @Test
    fun testProductInfoSerDes() {
        val applicationContext = startContext()

// tag::producer[]
        val productClient = applicationContext.getBean(ProductClient::class.java)
        productClient.send(ProductInfo("small", 10L, true))
        productClient.send(ProductInfo("medium", 20L, true))
        productClient.send(ProductInfo(null, 30L, false))
// end::producer[]

        val listener = applicationContext.getBean(ProductListener::class.java)

        try {
            await().atMost(5, TimeUnit.SECONDS).until {
                listener.messages.size == 3 &&
                        listener.messages.any { p -> p.size == "small" && p.count == 10L && p.sealed } &&
                        listener.messages.any { p -> p.size == "medium" && p.count == 20L && p.sealed } &&
                        listener.messages.any { p -> p.size == null && p.count == 30L && !p.sealed }
            }
        } finally {
            applicationContext.close()
        }
    }
}
