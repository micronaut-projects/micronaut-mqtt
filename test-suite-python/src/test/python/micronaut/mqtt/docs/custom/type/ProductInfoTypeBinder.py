# tag::imports[]
from jakarta.inject import Singleton
from micronaut.core.convert import ArgumentConversionContext, ConversionService
from micronaut.core.type import Argument
from micronaut.mqtt.bind import TypedMqttBinder
from micronaut.mqtt.v5.bind import MqttV5BindingContext
from org.eclipse.paho.mqttv5.common.packet import UserProperty

from java.lang import Boolean, Long
from java.util import Optional

from .ProductInfo import ProductInfo
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="ProductInfoSpec")
# tag::clazz[]
@Singleton  # <1>
class ProductInfoTypeBinder(TypedMqttBinder):  # <2>

    def __init__(self, conversion_service: ConversionService):  # <3>
        self.conversion_service = conversion_service

    def getArgumentType(self) -> Argument:
        return Argument.of(ProductInfo)

    def bindTo(self, context: MqttV5BindingContext, value: ProductInfo, argument: Argument) -> None:
        user_properties_list = context.getProperties().getUserProperties()
        if value.size is not None:
            user_properties_list.add(UserProperty("productSize", value.size))
        user_properties_list.add(UserProperty("productCount", str(value.count)))  # <4>
        user_properties_list.add(UserProperty("productSealed", str(value.sealed).lower()))

    def bindFrom(self, context: MqttV5BindingContext, conversion_context: ArgumentConversionContext) -> Optional:
        user_properties = {p.getKey(): p.getValue() for p in context.getProperties().getUserProperties()}
        size = user_properties.get("productSize")
        count = Optional.ofNullable(user_properties.get("productCount")).flatMap(lambda value: self.conversion_service.convert(value, Long))
        sealed = Optional.ofNullable(user_properties.get("productSealed")).flatMap(lambda value: self.conversion_service.convert(value, Boolean))

        if count.isPresent() and sealed.isPresent():
            return Optional.of(ProductInfo(size, count.get(), sealed.get()))  # <5>
        else:
            return Optional.empty()
# end::clazz[]
