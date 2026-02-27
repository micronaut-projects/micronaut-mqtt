package io.micronaut.mqtt.docs.parameters

import io.micronaut.mqtt.AbstractMQTTTest
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

class BindingSpec : AbstractMQTTTest() {

    @Test
    fun testProducerAndConsumer() {
        val applicationContext = startContext()

        // tag::producer[]
        val productClient = applicationContext.getBean(ProductClient::class.java)
        productClient.send("message body".toByteArray())
        productClient.send("product", "message body2".toByteArray())
        // end::producer[]

        val productListener = applicationContext.getBean(ProductListener::class.java)

        try {
            await().atMost(5, TimeUnit.SECONDS).until {
                productListener.messageLengths.size == 2 &&
                        productListener.messageLengths.contains(12) &&
                        productListener.messageLengths.contains(13)
            }
        } finally {
            applicationContext.close()
        }
    }
}
