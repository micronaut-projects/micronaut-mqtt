package io.micronaut.mqtt.exception;

import io.micronaut.messaging.exceptions.MessagingClientException;

public class MqttClientException extends MessagingClientException {

    public MqttClientException(String message) {
        super(message);
    }

    public MqttClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
