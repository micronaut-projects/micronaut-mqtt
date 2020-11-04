/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.mqtt.v3.intercept;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.mqtt.bind.MqttBinderRegistry;
import io.micronaut.mqtt.bind.MqttBindingContext;
import io.micronaut.mqtt.exception.MqttClientException;
import io.micronaut.mqtt.intercept.AbstractMqttIntroductionAdvice;
import io.micronaut.mqtt.v3.annotation.MqttClient;
import io.micronaut.mqtt.v3.bind.MqttV3BindingContext;
import org.eclipse.paho.client.mqttv3.*;

import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.util.function.Consumer;

/**
 * The MQTT v3 implementation of {@link AbstractMqttIntroductionAdvice}.
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@Singleton
public class MqttIntroductionAdvice extends AbstractMqttIntroductionAdvice<IMqttActionListener, MqttMessage> {

    private final MqttAsyncClient mqttAsyncClient;

    public MqttIntroductionAdvice(MqttAsyncClient mqttAsyncClient,
                                  MqttBinderRegistry binderRegistry) {
        super(binderRegistry);
        this.mqttAsyncClient = mqttAsyncClient;
    }

    @Override
    public MqttBindingContext<MqttMessage> createBindingContext(MethodInvocationContext<Object, Object> context) {
        return new MqttV3BindingContext(mqttAsyncClient, new MqttMessage());
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
