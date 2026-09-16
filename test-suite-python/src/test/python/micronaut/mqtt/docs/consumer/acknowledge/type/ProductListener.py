# tag::imports[]
from micronaut.messaging import Acknowledgement
from micronaut.mqtt.annotation import MqttSubscriber, Topic
# end::imports[]

from micronaut.context.annotation import Requires


@Requires(property="spec.name", value="AcknowledgeSpec")
# tag::clazz[]
@MqttSubscriber
class ProductListener:

    def __init__(self):
        self.message_count = 0

    @Topic("product")
    def receive(self, data: bytes, acknowledgement: Acknowledgement) -> None:  # <1>
        self.message_count += 1
        acknowledgement.ack()  # <2>
# end::clazz[]
