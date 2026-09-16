# tag::imports[]
from micronaut.mqtt.annotation import MqttSubscriber, Topic
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="PublisherAcknowledgeSpec")
# tag::clazz[]
@MqttSubscriber  # <1>
class ProductListener:

    def __init__(self):
        self.message_lengths: list[int] = []

    @Topic("product")  # <2>
    def receive(self, data: bytes) -> None:  # <3>
        length = len(data)
        self.message_lengths.append(length)
        print(f"Python received {length} bytes from MQTT")
# end::clazz[]
