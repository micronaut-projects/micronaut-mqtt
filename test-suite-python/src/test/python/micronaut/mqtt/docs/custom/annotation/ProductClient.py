from abc import ABC, abstractmethod
from typing import Annotated

# tag::imports[]
from micronaut.mqtt.annotation import Topic
from micronaut.mqtt.annotation.v5 import MqttPublisher

from .Correlation import Correlation
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="CorrelationSpec")
# tag::clazz[]
@MqttPublisher
class ProductClient(ABC):

    @Topic("product")
    @abstractmethod
    def send(self, correlation: Annotated[bytes, Correlation]) -> None:
        ...
# end::clazz[]
