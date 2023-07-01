package io.micronaut.mqtt3.graal;

import io.micronaut.mqtt.annotation.Topic;
import io.micronaut.mqtt.annotation.v3.MqttPublisher;

@MqttPublisher
public interface MessageConverterMqttClient {

    @Topic("message")
    void sendMessage(MessageRequest messageRequest);

}
