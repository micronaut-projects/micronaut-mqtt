from abc import ABC, abstractmethod

# tag::imports[]
from micronaut.mqtt.annotation import Topic
from micronaut.mqtt.annotation.v5 import MqttPublisher
from org.reactivestreams import Publisher
# end::imports[]

from java.util.concurrent import CompletableFuture
from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="PublisherAcknowledgeSpec")
# tag::clazz[]
@MqttPublisher
class ProductClient(ABC):

    @Topic("product")
    @abstractmethod
    def send_publisher(self, data: bytes) -> Publisher[None]:  # <1>
        ...

    @Topic("product")
    @abstractmethod
    def send_future(self, data: bytes) -> CompletableFuture[None]:  # <2>
        ...
# end::clazz[]
