# tag::imports[]
from micronaut.mqtt.annotation import MqttSubscriber, Topic

from .ProductInfo import ProductInfo
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="ProductInfoSpec")
# tag::clazz[]
@MqttSubscriber
class ProductListener:

    def __init__(self):
        self.messages: list[ProductInfo] = []

    @Topic("product")
    def receive(self, data: bytes,
                product_info: ProductInfo) -> None:  # <1>
        self.messages.append(product_info)
# end::clazz[]
