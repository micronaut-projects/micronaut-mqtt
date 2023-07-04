package io.micronaut.mqtt5.graal;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.micronaut.serde.annotation.Serdeable;

import java.time.LocalDateTime;

@Serdeable
public class MessageResponse {

    private final String text;

    private final LocalDateTime date;

    public MessageResponse(String text) {
        this.text = text.toUpperCase();
        this.date = LocalDateTime.now();
    }

    public String getText() {
        return text;
    }

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    public LocalDateTime getDate() {
        return date;
    }
}
