package io.micronaut.mqtt.docs.quickstart

import io.micronaut.mqtt.AbstractMQTTTest
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

class QuickstartSpec : AbstractMQTTTest() {

    @Test
    fun testProductClientAndListener() {
        val applicationContext = startContext()

// tag::producer[]
val productClient = applicationContext.getBean(ProductClient::class.java)
productClient.send("quickstart".toByteArray())
// end::producer[]

        val productListener = applicationContext.getBean(ProductListener::class.java)

        try {
            await().atMost(5, TimeUnit.SECONDS).until {
                productListener.messageLengths.size == 1 &&
                        productListener.messageLengths[0] == "quickstart"
            }
        } finally {
            applicationContext.close()
        }
    }
}
