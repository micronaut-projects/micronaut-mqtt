package io.micronaut.mqtt.docs.custom.annotation

import io.micronaut.mqtt.AbstractMQTTTest
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

class CorrelationSpec : AbstractMQTTTest() {

    @Test
    fun testCustomAnnotationBinder() {
        val applicationContext = startContext()

        // tag::producer[]
        val productClient = applicationContext.getBean(ProductClient::class.java)
        productClient.send("a".toByteArray())
        productClient.send("b".toByteArray())
        productClient.send("c".toByteArray())
        // end::producer[]

        val productListener = applicationContext.getBean(ProductListener::class.java)

        try {
            await().atMost(5, TimeUnit.SECONDS).until {
                productListener.messages.size == 3
            }
        } finally {
            applicationContext.close()
        }
    }
}
