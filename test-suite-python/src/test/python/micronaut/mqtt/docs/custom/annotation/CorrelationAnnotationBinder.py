import java

# tag::imports[]
from jakarta.inject import Singleton
from micronaut.core.convert import ArgumentConversionContext, ConversionService
from micronaut.core.type import Argument
from micronaut.mqtt.bind import AnnotatedMqttBinder
from micronaut.mqtt.v5.bind import MqttV5BindingContext

from java.util import Optional
# end::imports[]

from micronaut.context.annotation import Requires

# TODO(python): java.type needed because the annotation type is returned to Java as a runtime java.lang.Class
# (AnnotatedMqttBinder.getAnnotationType()); the Python annotation function itself is not accepted
# ("Cannot convert '<function Correlation>' to Java type 'java.lang.Class'")
CorrelationClass = java.type("micronaut.mqtt.docs.custom.annotation.Correlation")


@Requires(property="spec.name", value="CorrelationSpec")
# tag::clazz[]
@Singleton  # <1>
class CorrelationAnnotationBinder(AnnotatedMqttBinder):  # <2>

    def __init__(self, conversion_service: ConversionService):  # <3>
        self.conversion_service = conversion_service

    def getAnnotationType(self):
        return CorrelationClass

    def bindTo(self, context: MqttV5BindingContext, value: object, argument: Argument) -> None:
        context.getProperties().setCorrelationData(value)  # <4>

    def bindFrom(self, context: MqttV5BindingContext, conversion_context: ArgumentConversionContext) -> Optional:
        return self.conversion_service.convert(context.getProperties().getCorrelationData(), conversion_context)  # <5>
# end::clazz[]
