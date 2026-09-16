# tag::imports[]
from jakarta.inject import Singleton
from micronaut.core.convert import ConversionService
from micronaut.core.type import Argument
from micronaut.mqtt.serdes import MqttPayloadSerDes

from java.lang import Boolean, Long

from .ProductInfo import ProductInfo
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="ProductInfoSerDesSpec")
# tag::clazz[]
@Singleton  # <1>
class ProductInfoSerDes(MqttPayloadSerDes):  # <2>

    def __init__(self, conversion_service: ConversionService):  # <3>
        self.conversion_service = conversion_service

    def deserialize(self, payload: bytes, argument: Argument) -> ProductInfo | None:
        body = bytes(payload).decode("utf-8")
        parts = body.split("|")
        if len(parts) == 3:
            size = parts[0]
            if size == "null":
                size = None

            count = self.conversion_service.convert(parts[1], Long)
            sealed = self.conversion_service.convert(parts[2], Boolean)

            if count.isPresent() and sealed.isPresent():
                return ProductInfo(size, count.get(), sealed.get())  # <4>
        return None

    def serialize(self, data: ProductInfo | None) -> bytes | None:
        if data is None:
            return None
        size = "null" if data.size is None else data.size
        return f"{size}|{data.count}|{str(data.sealed).lower()}".encode("utf-8")  # <5>

    def supports(self, argument: Argument) -> bool:  # <6>
        return argument.getType().isAssignableFrom(ProductInfo)
# end::clazz[]
