package io.micronaut.mqtt.docs.custom.type

import io.kotest.assertions.timing.eventually
import io.kotest.matchers.collections.shouldExist
import io.kotest.matchers.shouldBe

import io.micronaut.mqtt.AbstractMqttKotest
import org.opentest4j.AssertionFailedError
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

@ExperimentalTime
class ProductInfoSpec : AbstractMqttKotest({

    val specName = javaClass.simpleName

    given("A custom type binder") {
        val ctx = startContext(specName)

        `when`("The messages are published") {
            val productListener = ctx.getBean(ProductListener::class.java)

            // tag::producer[]
            val productClient = ctx.getBean(ProductClient::class.java)
            productClient.send(ProductInfo("small", 10L, true))
            productClient.send(ProductInfo("medium", 20L, false))
            productClient.send(ProductInfo(null, 30L, true))
            // end::producer[]

            then("The messages are received") {
                eventually(10.toDuration(DurationUnit.SECONDS), AssertionFailedError::class) {
                    productListener.messages.size shouldBe 3
                    productListener.messages shouldExist { p -> p.size == "small" && p.count == 10L && p.sealed }
                    productListener.messages shouldExist { p -> p.size == "medium" && p.count == 20L && !p.sealed }
                    productListener.messages shouldExist { p -> p.size == null && p.count == 30L && p.sealed }
                }
            }
        }

        ctx.stop()
    }
})
