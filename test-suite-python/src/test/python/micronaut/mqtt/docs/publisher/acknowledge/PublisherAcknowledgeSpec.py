from time import sleep
from typing import Annotated

from jakarta.inject import Inject
from micronaut.context.annotation import Property
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test
from org.reactivestreams import Subscriber, Subscription

from .ProductClient import ProductClient
from .ProductListener import ProductListener


@Property(name="spec.name", value="PublisherAcknowledgeSpec")
@MicronautTest(environments=["mqtt"])
class PublisherAcknowledgeSpec:
    product_client: Annotated[ProductClient, Inject]
    listener: Annotated[ProductListener, Inject]

    @Test
    def test_publisher_acknowledgement(self):
        success_count = 0
        error_count = 0

        publisher = self.product_client.send_publisher(b"publisher body")
        future = self.product_client.send_future(b"future body")

        class VoidSubscriber(Subscriber):

            def onSubscribe(self, subscription: Subscription) -> None:
                subscription.request(1)

            def onNext(self, value: None) -> None:
                raise RuntimeError("Should never be called")

            def onError(self, throwable: Exception) -> None:
                # if an error occurs
                nonlocal error_count
                error_count += 1

            def onComplete(self) -> None:
                # if the publish was acknowledged
                nonlocal success_count
                success_count += 1

        publisher.subscribe(VoidSubscriber())

        def when_complete(value, throwable):
            nonlocal success_count, error_count
            if throwable is None:
                success_count += 1
            else:
                error_count += 1

        future.whenComplete(when_complete)

        for _ in range(50):
            if success_count == 2 and len(self.listener.message_lengths) == 2:
                break
            sleep(0.1)
        assert error_count == 0
        assert success_count == 2
        assert len(self.listener.message_lengths) == 2
