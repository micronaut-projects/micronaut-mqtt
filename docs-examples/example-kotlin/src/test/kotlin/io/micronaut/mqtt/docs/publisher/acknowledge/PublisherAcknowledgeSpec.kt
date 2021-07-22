package io.micronaut.mqtt.docs.publisher.acknowledge;

import io.kotest.assertions.timing.eventually
import io.kotest.matchers.shouldBe
import io.micronaut.mqtt.AbstractMqttKotest
import kotlinx.coroutines.async
import org.opentest4j.AssertionFailedError
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import java.util.concurrent.atomic.AtomicInteger
import kotlin.time.ExperimentalTime
import kotlin.time.seconds

@ExperimentalTime
class PublisherAcknowledgeSpec : AbstractMqttKotest({

    val specName = javaClass.simpleName

    given("Publisher acknowledgement") {
        val ctx = startContext(specName)
        val successCount = AtomicInteger(0)
        val errorCount = AtomicInteger(0)

        `when`("The messages are published") {
            val productClient = ctx.getBean(ProductClient::class.java)
            val publisher = productClient.sendPublisher("publisher body".toByteArray())
            val future = productClient.sendFuture("future body".toByteArray())
            val deferred = async {
                productClient.sendSuspend("suspend body".toByteArray())
            }

            val listener = ctx.getBean(ProductListener::class.java)

            val subscriber = (object : Subscriber<Void> {
                override fun onSubscribe(subscription: Subscription) { }

                override fun onNext(aVoid: Void) {
                    throw UnsupportedOperationException("Should never be called")
                }

                override fun onError(throwable: Throwable) {
                    // if an error occurs
                    errorCount.incrementAndGet()
                }

                override fun onComplete() {
                    // if the publish was acknowledged
                    successCount.incrementAndGet()
                }
            })
            publisher.subscribe(subscriber)
            future.handle { _, t ->
                if (t == null) {
                    successCount.incrementAndGet()
                } else {
                    errorCount.incrementAndGet()
                }
            }
            deferred.invokeOnCompletion {
                if (it == null) {
                    successCount.incrementAndGet()
                } else {
                    errorCount.incrementAndGet()
                }
            }

            then("The messages are published") {
                eventually(10.seconds, AssertionFailedError::class) {
                    errorCount.get() shouldBe 0
                    successCount.get() shouldBe 3
                    listener.messageLengths.size shouldBe 3
                }
            }
        }

        ctx.stop()
    }
})
