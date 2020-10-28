package io.micronaut.mqtt.exception;

import io.micronaut.messaging.exceptions.MessageListenerException;

public class MqttSubscriberException extends MessageListenerException {

    public MqttSubscriberException(String message) {
        super(message);
    }

    public MqttSubscriberException(String message, Throwable cause) {
        super(message, cause);
    }

}
