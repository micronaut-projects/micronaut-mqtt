package io.micronaut.mqtt.docs.quickstart

import io.kotest.assertions.timing.eventually
import io.kotest.matchers.shouldBe
import io.micronaut.mqtt.AbstractMqttKotest
import org.opentest4j.AssertionFailedError
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

@ExperimentalTime
class QuickstartSpec: AbstractMqttKotest({

    val specName = javaClass.simpleName

    given("A basic producer and consumer") {
        val ctx = startContext(specName)

        `when`("the message is published") {
            val productListener = ctx.getBean(ProductListener::class.java)

// tag::producer[]
val productClient = ctx.getBean(ProductClient::class.java)
productClient.send("quickstart".toByteArray())
// end::producer[]

            then("the message is consumed") {
                eventually(10.seconds, AssertionFailedError::class) {
                    productListener.messageLengths.size shouldBe 1
                    productListener.messageLengths[0] shouldBe "quickstart"
                }
            }
        }

        Thread.sleep(1000)
        ctx.stop()
    }

})
