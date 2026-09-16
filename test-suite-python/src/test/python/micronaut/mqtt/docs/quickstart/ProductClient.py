from abc import ABC, abstractmethod

# tag::imports[]
from micronaut.mqtt.annotation import Topic
from micronaut.mqtt.annotation.v5 import MqttPublisher
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="QuickstartSpec")
# tag::clazz[]
@MqttPublisher  # <1>
class ProductClient(ABC):

    @Topic("product")  # <2>
    @abstractmethod
    def send(self, data: bytes) -> None:  # <3>
        ...
# end::clazz[]
