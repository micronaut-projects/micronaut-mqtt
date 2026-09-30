from abc import ABC, abstractmethod
from typing import Annotated

# tag::imports[]
from micronaut.mqtt.annotation import Topic
from micronaut.mqtt.annotation.v5 import MqttProperty, MqttPublisher
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="PropertiesSpec")
# tag::clazz[]
@MqttPublisher
@MqttProperty(name="appId", value="myApp")  # <1>
class ProductClient(ABC):

    @Topic("product")
    @MqttProperty(name="contentType", value="application/json")  # <2>
    @MqttProperty(name="userId", value="guest")
    @abstractmethod
    def send(self, data: bytes) -> None:
        ...

    @Topic("product")
    @abstractmethod
    def send_with_properties(self, user: Annotated[str, MqttProperty("userId")], contentType: Annotated[str | None, MqttProperty], data: bytes) -> None:  # <3>
        ...
# end::clazz[]
