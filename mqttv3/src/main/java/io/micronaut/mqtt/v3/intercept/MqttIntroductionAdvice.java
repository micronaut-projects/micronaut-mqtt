package io.micronaut.mqtt.v3.intercept;

import io.micronaut.core.convert.ConversionService;
import io.micronaut.mqtt.bind.MqttBinderRegistry;
import io.micronaut.mqtt.exception.MqttClientException;
import io.micronaut.mqtt.intercept.AbstractMqttIntroductionAdvice;
import io.micronaut.mqtt.serdes.MqttPayloadSerDesRegistry;
import io.micronaut.mqtt.v3.annotation.MqttClient;
import io.micronaut.mqtt.v3.bind.MqttV3Message;
import org.eclipse.paho.client.mqttv3.*;

import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.util.function.Consumer;

@Singleton
public class MqttIntroductionAdvice extends AbstractMqttIntroductionAdvice<IMqttActionListener, MqttMessage> {

    private final MqttAsyncClient mqttAsyncClient;

    public MqttIntroductionAdvice(MqttAsyncClient mqttAsyncClient,
                                  ConversionService<?> conversionService,
                                  MqttPayloadSerDesRegistry serDesRegistry,
                                  MqttBinderRegistry binderRegistry) {
        super(conversionService, serDesRegistry, binderRegistry);
        this.mqttAsyncClient = mqttAsyncClient;
    }

    @Override
    public io.micronaut.mqtt.bind.MqttMessage<MqttMessage> createMessage() {
        return new MqttV3Message(new MqttMessage());
    }

    @Override
    public Object publish(String topic, MqttMessage message, IMqttActionListener listener) {
        try {
            return mqttAsyncClient.publish(topic, message, null, listener);
        } catch (MqttException e) {
            throw new MqttClientException("Failed to publish the message", e);
        }
    }

    @Override
    public IMqttActionListener createListener(Runnable onSuccess, Consumer<Throwable> onError) {
        return new IMqttActionListener() {
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
