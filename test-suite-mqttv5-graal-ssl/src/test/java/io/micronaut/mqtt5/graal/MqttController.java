package io.micronaut.mqtt5.graal;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

import java.util.List;

@Controller("/mqtt")
public class MqttController {

    private final MessageConverterMqttClient messageConverterMqttClient;
    private final MessageListener messageListener;

    public MqttController(MessageConverterMqttClient messageConverterMqttClient,
                          MessageListener messageListener) {
        this.messageConverterMqttClient = messageConverterMqttClient;
        this.messageListener = messageListener;
    }

    @Get("/send/{text}")
    public void send(String text) {
        messageConverterMqttClient.sendMessage(new MessageRequest(text));
    }

    @Get("/messages")
    public List<MessageResponse> messages() {
        return messageListener.getMessages();
    }

}
