package io.micronaut.mqtt5.graal;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class MessageRequest {

    private final String text;

    public MessageRequest(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
