from abc import ABC, abstractmethod
from typing import Annotated

# tag::imports[]
from micronaut.messaging.annotation import MessageBody
from micronaut.mqtt.annotation import Topic
from micronaut.mqtt.annotation.v5 import MqttPublisher

from .ProductInfo import ProductInfo
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="ProductInfoSerDesSpec")
# tag::clazz[]
@MqttPublisher
class ProductClient(ABC):

    @Topic("product")
    @abstractmethod
    def send(self, data: Annotated[ProductInfo, MessageBody]) -> None:
        ...
# end::clazz[]
