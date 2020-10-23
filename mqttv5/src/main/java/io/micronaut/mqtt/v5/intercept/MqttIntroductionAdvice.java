package io.micronaut.mqtt.v5.intercept;

import io.micronaut.core.convert.ConversionService;
import io.micronaut.mqtt.bind.MqttBinderRegistry;
import io.micronaut.mqtt.exception.MqttClientException;
import io.micronaut.mqtt.intercept.AbstractMqttIntroductionAdvice;
import io.micronaut.mqtt.serdes.MqttPayloadSerDesRegistry;
import io.micronaut.mqtt.v5.annotation.MqttClient;
import io.micronaut.mqtt.v5.bind.MqttV5Message;
import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttActionListener;
import org.eclipse.paho.mqttv5.client.MqttAsyncClient;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;

import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.util.function.Consumer;

@Singleton
public class MqttIntroductionAdvice extends AbstractMqttIntroductionAdvice<MqttActionListener, MqttMessage> {

    private final MqttAsyncClient mqttAsyncClient;

    public MqttIntroductionAdvice(MqttAsyncClient mqttAsyncClient,
                                  ConversionService<?> conversionService,
                                  MqttPayloadSerDesRegistry serDesRegistry,
                                  MqttBinderRegistry binderRegistry) {
        super(conversionService, serDesRegistry, binderRegistry);
        this.mqttAsyncClient = mqttAsyncClient;
    }

    @Override
    public Object publish(String topic, MqttMessage message, MqttActionListener listener) {
        try {
            return mqttAsyncClient.publish(topic, message, null, listener);
        } catch (MqttException e) {
            throw new MqttClientException("Failed to publish the message", e);
        }
    }

    @Override
    public io.micronaut.mqtt.bind.MqttMessage<MqttMessage> createMessage() {
        return new MqttV5Message(new MqttMessage());
    }

    @Override
    public MqttActionListener createListener(Runnable onSuccess, Consumer<Throwable> onError) {
        return new MqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                onSuccess.run();
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                onError.accept(exception);
            }
        };
    }

    @Override
    public Class<? extends Annotation> getRequiredAnnotation() {
        return MqttClient.class;
    }

}
