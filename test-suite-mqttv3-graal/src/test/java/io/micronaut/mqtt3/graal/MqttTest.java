package io.micronaut.mqtt3.graal;

import io.micronaut.context.annotation.Property;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest
class MqttTest {

    @Inject
    @Client("/")
    HttpClient httpClient;

    @Test
    void helloWorld() {
        HttpResponse<Object> exchange = httpClient.toBlocking().exchange("/mqtt/send/Micronaut");
        assertEquals(200, exchange.getStatus().getCode());
        await().atMost(5, TimeUnit.SECONDS).until(() -> {
            List<MessageResponse> exchange1 = httpClient.toBlocking().exchange(HttpRequest.GET("/mqtt/messages"), Argument.listOf(MessageResponse.class)).body();
            return exchange1 != null && exchange1.size() == 1 && exchange1.get(0).getText().equals("MICRONAUT");
        });
    }

    @Introspected
    static class MessageResponse {
        private final String text;
        private final LocalDate date;

        public MessageResponse(String text, LocalDate date) {
            this.text = text;
            this.date = date;
        }

        public String getText() {
            return text;
        }

        public LocalDate getDate() {
            return date;
        }

        @Override
        public String toString() {
            return "MessageResponse{" +
                "text='" + text + '\'' +
                ", date=" + date +
                '}';
        }
    }
}
