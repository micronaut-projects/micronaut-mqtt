package io.micronaut.mqtt.docs.parameters

import io.kotest.assertions.timing.eventually
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import io.micronaut.mqtt.AbstractMqttKotest
import org.opentest4j.AssertionFailedError
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

@ExperimentalTime
class BindingSpec: AbstractMqttKotest({

    val specName = javaClass.simpleName

    given("A basic producer and consumer") {
        val ctx = startContext(specName)

        `when`("The messages are published") {
            val productListener = ctx.getBean(ProductListener::class.java)

            // tag::producer[]
            val productClient = ctx.getBean(ProductClient::class.java)
            productClient.send("message body".toByteArray())
            productClient.send("product", "message body2".toByteArray())
            // end::producer[]

            then("The messages are received") {
                eventually(10.seconds, AssertionFailedError::class) {
                    productListener.messageLengths.size shouldBe 2
                    productListener.messageLengths shouldContain 12
                    productListener.messageLengths shouldContain 13
                }
            }
        }

        ctx.stop()
    }
})
