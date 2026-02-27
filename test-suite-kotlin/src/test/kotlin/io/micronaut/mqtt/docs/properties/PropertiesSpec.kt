package io.micronaut.mqtt.docs.properties

import io.micronaut.mqtt.AbstractMQTTTest
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

class PropertiesSpec : AbstractMQTTTest() {

    @Test
    fun testPublishingAndReceivingProperties() {
        val applicationContext = startContext()

        // tag::producer[]
        val productClient = applicationContext.getBean(ProductClient::class.java)
        productClient.send("body".toByteArray())
        productClient.send("guest", "text/html", "body2".toByteArray())
        productClient.send("guest", null, "body3".toByteArray())
        // end::producer[]

        val productListener = applicationContext.getBean(ProductListener::class.java)

        try {
            await().atMost(5, TimeUnit.SECONDS).until {
                productListener.messageProperties.size == 3 &&
                        productListener.messageProperties.contains("guest|application/json|myApp") &&
                        productListener.messageProperties.contains("guest|text/html|myApp") &&
                        productListener.messageProperties.contains("guest|null|myApp")
            }
        } finally {
            applicationContext.close()
        }
    }
}
