from time import sleep
from typing import Annotated

from jakarta.inject import Inject
from micronaut.context.annotation import Property
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test

from .ProductClient import ProductClient
from .ProductListener import ProductListener


@Property(name="spec.name", value="CorrelationSpec")
@MicronautTest(environments=["mqtt"])
class CorrelationSpec:
    product_client: Annotated[ProductClient, Inject]
    product_listener: Annotated[ProductListener, Inject]

    @Test
    def test_using_a_custom_annotation_binder(self):
        # tag::producer[]
        self.product_client.send(b"a")
        self.product_client.send(b"b")
        self.product_client.send(b"c")
        # end::producer[]

        expected_messages = {"a", "b", "c"}
        for _ in range(50):
            if self.product_listener.messages == expected_messages:
                break
            sleep(0.1)
        assert self.product_listener.messages == expected_messages
