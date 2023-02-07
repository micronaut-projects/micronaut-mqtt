package io.micronaut.mqtt3.graal;

import io.micronaut.mqtt.annotation.Topic;
import io.micronaut.mqtt.v3.annotation.MqttPublisher;

@MqttPublisher
public interface MessageConverterMqttClient {

    @Topic("message")
    void sendMessage(MessageRequest messageRequest);

}
