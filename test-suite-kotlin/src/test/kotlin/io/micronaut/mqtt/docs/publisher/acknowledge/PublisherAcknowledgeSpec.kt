package io.micronaut.mqtt.docs.publisher.acknowledge

import io.micronaut.mqtt.AbstractMQTTTest
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class PublisherAcknowledgeSpec : AbstractMQTTTest() {

    @Test
    fun testPublisherAcknowledgement() {
        val applicationContext = startContext()
        val successCount = AtomicInteger(0)
        val errorCount = AtomicInteger(0)

        val productClient = applicationContext.getBean(ProductClient::class.java)
        val publisher = productClient.sendPublisher("publisher body".toByteArray())
        val future = productClient.sendFuture("future body".toByteArray())
        val listener = applicationContext.getBean(ProductListener::class.java)

        val subscriber = object : Subscriber<Void> {
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
        }
        publisher.subscribe(subscriber)
        future.whenComplete { _, t ->
            if (t == null) {
                successCount.incrementAndGet()
            } else {
                errorCount.incrementAndGet()
            }
        }

        try {
            await().atMost(5, TimeUnit.SECONDS).until {
                errorCount.get() == 0 &&
                        successCount.get() == 2 &&
                        listener.messageLengths.size == 2
            }
        } finally {
            applicationContext.close()
        }
    }
}
