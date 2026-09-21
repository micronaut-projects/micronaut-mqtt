import java

# tag::imports[]
from jakarta.inject import Singleton
from micronaut.core.convert import ArgumentConversionContext, ConversionService
from micronaut.core.type import Argument
from micronaut.mqtt.bind import AnnotatedMqttBinder
from micronaut.mqtt.v5.bind import MqttV5BindingContext

from java.util import Optional

from .Correlation import Correlation
# end::imports[]

from micronaut.context.annotation import Requires

# TODO(python): java.type needed because the annotation type is returned to Java as a runtime java.lang.Class
# (AnnotatedMqttBinder.getAnnotationType()); a Python-defined annotation function is not accepted
# ("Cannot convert '<function Correlation>' (language: Python, type: function) to Java type 'java.lang.Class'")
CorrelationClass = java.type("micronaut.mqtt.docs.custom.annotation.Correlation")


@Requires(property="spec.name", value="CorrelationSpec")
# tag::clazz[]
@Singleton  # <1>
class CorrelationAnnotationBinder(AnnotatedMqttBinder[MqttV5BindingContext, Correlation]):  # <2>

    def __init__(self, conversion_service: ConversionService):  # <3>
        self.conversion_service = conversion_service

    def getAnnotationType(self) -> type[Correlation]:
        return CorrelationClass

    def bindTo(self, context: MqttV5BindingContext, value: object, argument: Argument[object]) -> None:
        context.getProperties().setCorrelationData(value)  # <4>

    def bindFrom(self, context: MqttV5BindingContext, conversion_context: ArgumentConversionContext[object]) -> Optional[object]:
        return self.conversion_service.convert(context.getProperties().getCorrelationData(), conversion_context)  # <5>
# end::clazz[]
