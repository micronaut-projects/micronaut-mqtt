package io.micronaut.mqtt.docs.custom.annotation

import io.kotest.assertions.timing.eventually
import io.kotest.matchers.shouldBe
import io.micronaut.mqtt.AbstractMqttKotest
import org.opentest4j.AssertionFailedError
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

@ExperimentalTime
class CorrelationSpec : AbstractMqttKotest({

    val specName = javaClass.simpleName

    given("Using a custom annotation binder") {
        val ctx = startContext(specName)

        `when`("The messages are published") {
            val productListener = ctx.getBean(ProductListener::class.java)

            // tag::producer[]
            val productClient = ctx.getBean(ProductClient::class.java)
            productClient.send("a".toByteArray())
            productClient.send("b".toByteArray())
            productClient.send("c".toByteArray())
            // end::producer[]

            then("The messages are received") {
                eventually(10.seconds, AssertionFailedError::class) {
                    productListener.messages.size shouldBe 3
                }
            }
        }

        ctx.stop()
    }
})
