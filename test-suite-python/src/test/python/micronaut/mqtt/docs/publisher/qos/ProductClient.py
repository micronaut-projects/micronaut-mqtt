from abc import ABC, abstractmethod
from typing import Annotated

# tag::imports[]
from micronaut.mqtt.annotation import Qos, Topic
from micronaut.mqtt.annotation.v5 import MqttPublisher
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="PublisherQosSpec")
# tag::clazz[]
@MqttPublisher
class ProductClient(ABC):

    @Topic(value="product", qos=2)  # <1>
    @abstractmethod
    def send(self, data: bytes) -> None:
        ...

    @Topic("product")
    @abstractmethod
    def send_with_qos(self, data: bytes, qos: Annotated[int, Qos]) -> None:  # <2>
        ...
# end::clazz[]
