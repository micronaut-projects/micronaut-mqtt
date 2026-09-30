from time import sleep
from typing import Annotated

from jakarta.inject import Inject
from micronaut.context.annotation import Property
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test
from org.reactivestreams import Subscriber, Subscription

from .ProductClient import ProductClient
from .ProductListener import ProductListener


# TODO(python): a class defined inside a method cannot extend an imported Java interface with core 5.2.3
# ("TypeError: invalid instantiation of foreign object" when it is instantiated; it worked with the generated
# import modules of 5.2.2), so the subscriber of the Java example's anonymous class is a module-level class.
class VoidSubscriber(Subscriber):

    def __init__(self, counts: dict[str, int]) -> None:
        self.counts = counts

    def onSubscribe(self, subscription: Subscription) -> None:
        subscription.request(1)

    def onNext(self, value: None) -> None:
        raise RuntimeError("Should never be called")

    def onError(self, throwable: Exception) -> None:
        # if an error occurs
        self.counts["error"] += 1

    def onComplete(self) -> None:
        # if the publish was acknowledged
        self.counts["success"] += 1


@Property(name="spec.name", value="PublisherAcknowledgeSpec")
@MicronautTest(environments=["mqtt"])
class PublisherAcknowledgeSpec:
    product_client: Annotated[ProductClient, Inject]
    listener: Annotated[ProductListener, Inject]

    @Test
    def test_publisher_acknowledgement(self):
        counts = {"success": 0, "error": 0}

        publisher = self.product_client.send_publisher(b"publisher body")
        future = self.product_client.send_future(b"future body")

        publisher.subscribe(VoidSubscriber(counts))

        def when_complete(value, throwable):
            if throwable is None:
                counts["success"] += 1
            else:
                counts["error"] += 1

        future.whenComplete(when_complete)

        for _ in range(50):
            if counts["success"] == 2 and len(self.listener.message_lengths) == 2:
                break
            sleep(0.1)
        assert counts["error"] == 0
        assert counts["success"] == 2
        assert len(self.listener.message_lengths) == 2
