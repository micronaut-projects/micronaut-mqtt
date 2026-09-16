from abc import ABC, abstractmethod
from typing import Annotated

# tag::imports[]
from micronaut.mqtt.annotation import Topic
from micronaut.mqtt.annotation.v5 import MqttPublisher
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="BindingSpec")
# tag::clazz[]
@MqttPublisher
class ProductClient(ABC):

    @Topic("product")  # <1>
    @abstractmethod
    def send(self, data: bytes) -> None:
        ...

    @abstractmethod
    def send_to_topic(self, binding: Annotated[str, Topic], data: bytes) -> None:  # <2>
        ...
# end::clazz[]
