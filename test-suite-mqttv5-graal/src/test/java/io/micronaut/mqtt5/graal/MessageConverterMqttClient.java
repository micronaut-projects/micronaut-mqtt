package io.micronaut.mqtt5.graal;

import io.micronaut.mqtt.annotation.Topic;
import io.micronaut.mqtt.v5.annotation.MqttPublisher;

@MqttPublisher
public interface MessageConverterMqttClient {

    @Topic("message")
    void sendMessage(MessageRequest messageRequest);

}
