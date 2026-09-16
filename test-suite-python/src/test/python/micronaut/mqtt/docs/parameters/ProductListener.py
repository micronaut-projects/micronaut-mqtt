from typing import Annotated

# tag::imports[]
from micronaut.mqtt.annotation import MqttSubscriber, Topic
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="BindingSpec")
# tag::clazz[]
@MqttSubscriber
class ProductListener:

    def __init__(self):
        self.message_lengths: list[int] = []
        self.topics: list[str] = []

    @Topic("product")  # <1>
    def receive(self, data: bytes) -> None:
        self.message_lengths.append(len(data))

    @Topic("product/a")
    @Topic("product/b")  # <2>
    def receive_from_topic(self, data: bytes, topic: Annotated[str, Topic]) -> None:
        self.topics.append(topic)
# end::clazz[]
