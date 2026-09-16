from time import sleep
from typing import Annotated

from jakarta.inject import Inject
from micronaut.context.annotation import Property
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test

from .ProductClient import ProductClient
from .ProductListener import ProductListener


@Property(name="spec.name", value="BindingSpec")
@MicronautTest(environments=["mqtt"])
class BindingSpec:
    product_client: Annotated[ProductClient, Inject]
    product_listener: Annotated[ProductListener, Inject]

    @Test
    def test_dynamic_binding(self):
        # tag::producer[]
        self.product_client.send(b"message body")
        self.product_client.send_to_topic("product/a", b"message body2")
        self.product_client.send_to_topic("product/b", b"message body2")
        # end::producer[]

        listener = self.product_listener
        for _ in range(50):
            if len(listener.message_lengths) == 1 and len(listener.topics) == 2:
                break
            sleep(0.1)
        assert listener.message_lengths == [12]
        assert listener.topics == ["product/a", "product/b"]
