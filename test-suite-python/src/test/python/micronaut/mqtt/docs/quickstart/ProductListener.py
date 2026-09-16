# tag::imports[]
from micronaut.mqtt.annotation import MqttSubscriber, Topic
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="QuickstartSpec")
# tag::clazz[]
@MqttSubscriber  # <1>
class ProductListener:

    def __init__(self):
        self.message_lengths: list[str] = []

    @Topic("product")  # <2>
    def receive(self, data: bytes) -> None:  # <3>
        self.message_lengths.append(bytes(data).decode())
        print(f"Python received {len(data)} bytes from MQTT")
# end::clazz[]
