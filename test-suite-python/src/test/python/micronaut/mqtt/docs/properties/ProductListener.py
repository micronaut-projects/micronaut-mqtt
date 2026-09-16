from typing import Annotated

# tag::imports[]
from micronaut.mqtt.annotation import MqttSubscriber, Topic
from micronaut.mqtt.annotation.v5 import MqttProperty
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="PropertiesSpec")
# tag::clazz[]
@MqttSubscriber
class ProductListener:

    def __init__(self):
        self.message_properties: list[str] = []

    @Topic("product")
    def receive(self, data: bytes,
                user: Annotated[str, MqttProperty("userId")],  # <1>
                contentType: Annotated[str | None, MqttProperty],  # <2>
                appId: Annotated[str, MqttProperty]) -> None:  # <3>
        self.message_properties.append(f"{user}|{contentType}|{appId}")
# end::clazz[]
