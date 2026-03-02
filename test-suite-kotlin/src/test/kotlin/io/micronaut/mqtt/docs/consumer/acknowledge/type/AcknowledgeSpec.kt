package io.micronaut.mqtt.docs.consumer.acknowledge.type

import io.micronaut.mqtt.AbstractMQTTTest
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

class AcknowledgeSpec : AbstractMQTTTest() {

    @Test
    fun testAcknowledgeArgument() {
        val applicationContext = startContext()

        // tag::producer[]
        val productClient = applicationContext.getBean(ProductClient::class.java)
        productClient.send("body".toByteArray())
        productClient.send("body".toByteArray())
        productClient.send("body".toByteArray())
        productClient.send("body".toByteArray())
        // end::producer[]

        val productListener = applicationContext.getBean(ProductListener::class.java)

        try {
            await().atMost(5, TimeUnit.SECONDS).until {
                productListener.messageCount.get() == 4
            }
        } finally {
            applicationContext.close()
        }
    }
}
