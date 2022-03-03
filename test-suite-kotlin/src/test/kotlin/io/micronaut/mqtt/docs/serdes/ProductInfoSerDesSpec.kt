package io.micronaut.mqtt.docs.serdes

import io.kotest.assertions.timing.eventually
import io.kotest.matchers.collections.shouldExist
import io.kotest.matchers.shouldBe
import io.micronaut.mqtt.AbstractMqttKotest
import org.opentest4j.AssertionFailedError
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

@ExperimentalTime
class ProductInfoSerDesSpec: AbstractMqttKotest({

    val specName = javaClass.simpleName

    given("A basic producer and consumer") {
        val ctx = startContext(specName)

        `when`("the message is published") {
            val listener = ctx.getBean(ProductListener::class.java)

// tag::producer[]
            val productClient = ctx.getBean(ProductClient::class.java)
            productClient.send(ProductInfo("small", 10L, true))
            productClient.send(ProductInfo("medium", 20L, true))
            productClient.send(ProductInfo(null, 30L, false))
// end::producer[]

            then("the message is consumed") {
                eventually(10.toDuration(DurationUnit.SECONDS), AssertionFailedError::class) {
                    listener.messages.size shouldBe 3
                    listener.messages shouldExist { p -> p.size == "small" && p.count == 10L && p.sealed }
                    listener.messages shouldExist { p -> p.size == "medium" && p.count == 20L && p.sealed }
                    listener.messages shouldExist { p -> p.size == null && p.count == 30L && !p.sealed }
                }
            }
        }

        ctx.stop()
    }

})
