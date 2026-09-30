from typing import Annotated

# tag::imports[]
from micronaut.mqtt.annotation import MqttSubscriber, Topic

from .Correlation import Correlation
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="CorrelationSpec")
# tag::clazz[]
@MqttSubscriber
class ProductListener:

    def __init__(self):
        self.messages: set[str] = set()

    @Topic("product")
    def receive(self, data: bytes, correlation: Annotated[bytes, Correlation]) -> None:  # <1>
        self.messages.add(bytes(correlation).decode())
# end::clazz[]
