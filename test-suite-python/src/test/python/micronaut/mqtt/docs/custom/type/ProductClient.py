from abc import ABC, abstractmethod

# tag::imports[]
from micronaut.mqtt.annotation import Topic
from micronaut.mqtt.annotation.v5 import MqttPublisher

from .ProductInfo import ProductInfo
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="ProductInfoSpec")
# tag::clazz[]
@MqttPublisher
class ProductClient(ABC):

    @Topic("product")
    @abstractmethod
    def send(self, data: bytes, product_info: ProductInfo) -> None:
        ...
# end::clazz[]
