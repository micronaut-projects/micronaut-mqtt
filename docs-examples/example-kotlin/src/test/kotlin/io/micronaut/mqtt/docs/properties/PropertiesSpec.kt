package io.micronaut.mqtt.docs.properties

import io.kotest.assertions.timing.eventually
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.micronaut.mqtt.AbstractMqttKotest
import org.opentest4j.AssertionFailedError
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

@ExperimentalTime
class PropertiesSpec : AbstractMqttKotest({

    val specName = javaClass.simpleName

    given("publishing and receiving properties") {
        val ctx = startContext(specName)

        `when`("messages with properties are sent") {
            // tag::producer[]
            val productClient = ctx.getBean(ProductClient::class.java)
            productClient.send("body".toByteArray())
            productClient.send("guest", "text/html", "body2".toByteArray())
            productClient.send("guest", null, "body3".toByteArray())
            // end::producer[]

            then("the messages are received") {
                val productListener = ctx.getBean(ProductListener::class.java)

                eventually(10.seconds, AssertionFailedError::class) {
                    productListener.messageProperties.size shouldBe 3
                    productListener.messageProperties shouldContain "guest|application/json|myApp"
                    productListener.messageProperties shouldContain "guest|text/html|myApp"
                    productListener.messageProperties shouldContain "guest|null|myApp"
                }
            }
        }

        ctx.stop()
    }
})
