from time import sleep
from typing import Annotated

from jakarta.inject import Inject
from micronaut.context.annotation import Property
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test

from .ProductInfo import ProductInfo
from .ProductClient import ProductClient
from .ProductListener import ProductListener


@Property(name="spec.name", value="ProductInfoSpec")
@MicronautTest(environments=["mqtt"])
class ProductInfoSpec:
    product_client: Annotated[ProductClient, Inject]
    product_listener: Annotated[ProductListener, Inject]

    @Test
    def test_using_a_custom_type_binder(self):
        # tag::producer[]
        self.product_client.send(b"body", ProductInfo("small", 10, True))
        self.product_client.send(b"body2", ProductInfo("medium", 20, True))
        self.product_client.send(b"body3", ProductInfo(None, 30, True))
        # end::producer[]

        for _ in range(50):
            if len(self.product_listener.messages) == 3:
                break
            sleep(0.1)
        counts = [pi.count for pi in self.product_listener.messages]
        assert len(counts) == 3
        assert 10 in counts
        assert 20 in counts
        assert 30 in counts
