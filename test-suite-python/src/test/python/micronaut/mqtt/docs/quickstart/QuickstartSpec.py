from time import sleep
from typing import Annotated

from jakarta.inject import Inject
from micronaut.context.annotation import Property
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test

from .ProductClient import ProductClient
from .ProductListener import ProductListener


@Property(name="spec.name", value="QuickstartSpec")
@MicronautTest(environments=["mqtt"])
class QuickstartSpec:
    product_client: Annotated[ProductClient, Inject]
    product_listener: Annotated[ProductListener, Inject]

    @Test
    def test_product_client_and_listener(self):
        # tag::producer[]
        self.product_client.send(b"quickstart")
        # end::producer[]

        for _ in range(50):
            if self.product_listener.message_lengths == ["quickstart"]:
                break
            sleep(0.1)
        assert self.product_listener.message_lengths == ["quickstart"]
