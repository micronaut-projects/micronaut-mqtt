from abc import ABC, abstractmethod
from typing import Annotated

# tag::imports[]
from micronaut.mqtt.annotation import Retained, Topic
from micronaut.mqtt.annotation.v5 import MqttPublisher
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="PublisherQosSpec")
# tag::clazz[]
@MqttPublisher
class ProductClient(ABC):

    @Topic("product")
    @Retained(True)  # <1>
    @abstractmethod
    def send(self, data: bytes) -> None:
        ...

    @Topic("product")
    @abstractmethod
    def send_retained(self, data: bytes, retained: Annotated[bool, Retained]) -> None:  # <2>
        ...
# end::clazz[]
