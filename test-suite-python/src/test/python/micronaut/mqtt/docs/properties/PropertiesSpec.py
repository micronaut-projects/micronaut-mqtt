from time import sleep
from typing import Annotated

from jakarta.inject import Inject
from micronaut.context.annotation import Property
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test

from .ProductClient import ProductClient
from .ProductListener import ProductListener


@Property(name="spec.name", value="PropertiesSpec")
@MicronautTest(environments=["mqtt"])
class PropertiesSpec:
    product_client: Annotated[ProductClient, Inject]
    product_listener: Annotated[ProductListener, Inject]

    @Test
    def test_publishing_and_receiving_properties(self):
        # tag::producer[]
        self.product_client.send(b"body")
        self.product_client.send_with_properties("guest", "text/html", b"body2")
        self.product_client.send_with_properties("guest", None, b"body3")
        # end::producer[]

        listener = self.product_listener
        for _ in range(50):
            if len(listener.message_properties) == 3:
                break
            sleep(0.1)
        assert len(listener.message_properties) == 3
        assert "guest|application/json|myApp" in listener.message_properties
        assert "guest|text/html|myApp" in listener.message_properties
        assert "guest|None|myApp" in listener.message_properties
