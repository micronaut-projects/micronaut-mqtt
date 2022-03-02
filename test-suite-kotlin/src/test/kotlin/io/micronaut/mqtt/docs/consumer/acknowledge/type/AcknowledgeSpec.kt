package io.micronaut.mqtt.docs.consumer.acknowledge.type

import io.kotest.assertions.timing.eventually
import io.kotest.matchers.shouldBe
import io.micronaut.mqtt.AbstractMqttKotest
import org.opentest4j.AssertionFailedError
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

@ExperimentalTime
class AcknowledgeSpec : AbstractMqttKotest({

    val specName = javaClass.simpleName

    given("An acknowledgement argument") {
        val ctx = startContext(specName)

        `when`("The messages are published") {
            val productListener = ctx.getBean(ProductListener::class.java)

            // tag::producer[]
            val productClient = ctx.getBean(ProductClient::class.java)
            productClient.send("body".toByteArray())
            productClient.send("body".toByteArray())
            productClient.send("body".toByteArray())
            productClient.send("body".toByteArray())
            // end::producer[]

            then("The messages are received") {
                eventually(10.toDuration(DurationUnit.SECONDS), AssertionFailedError::class) {
                    productListener.messageCount.get() shouldBe 4
                }
            }
        }

        ctx.stop()
    }
})
