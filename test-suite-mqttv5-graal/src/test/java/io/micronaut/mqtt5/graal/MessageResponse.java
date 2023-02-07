package io.micronaut.mqtt5.graal;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.micronaut.core.annotation.Introspected;

import java.time.LocalDateTime;

@Introspected
public class MessageResponse {

    private final String text;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime date;

    public MessageResponse(String text) {
        this.text = text.toUpperCase();
        this.date = LocalDateTime.now();
    }

    public String getText() {
        return text;
    }

    public LocalDateTime getDate() {
        return date;
    }
}
