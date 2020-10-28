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

import io.micronaut.context.BeanContext;
import io.micronaut.mqtt.bind.MqttBinderRegistry;
import io.micronaut.mqtt.bind.MqttBindingContext;
import io.micronaut.mqtt.exception.MqttSubscriberException;
import io.micronaut.mqtt.intercept.AbstractMqttSubscriberAdvice;
import io.micronaut.mqtt.v3.bind.MqttV3BindingContext;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.Set;
import java.util.function.Consumer;

@Singleton
public class MqttSubscriberAdvice extends AbstractMqttSubscriberAdvice<MqttMessage> {

    private static final Logger LOG = LoggerFactory.getLogger(MqttSubscriberAdvice.class);
    private final MqttAsyncClient mqttAsyncClient;

    public MqttSubscriberAdvice(BeanContext beanContext,
                                MqttBinderRegistry binderRegistry,
                                MqttAsyncClient mqttAsyncClient) {
        super(beanContext, binderRegistry);
        this.mqttAsyncClient = mqttAsyncClient;
    }

    @Override
    public void subscribe(String topic, int qos, Consumer<MqttBindingContext<MqttMessage>> callback) {
        try {
            mqttAsyncClient.subscribe(topic, qos, (actualTopic, message) -> {
                MqttV3BindingContext context = new MqttV3BindingContext(message);
                context.setTopic(actualTopic);
                callback.accept(context);
            });
        } catch (MqttException e) {
            throw new MqttSubscriberException(String.format("Failed to subscribe to the topic: %s", topic), e);
        }
    }

    @Override
    public void unsubscribe(Set<String> topics) {
        try {
            IMqttToken token = mqttAsyncClient.unsubscribe(topics.toArray(new String[]{}));
            token.waitForCompletion();
        } catch (MqttException e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Failed to unsubscribe from the subscribed topics", e);
            }
        }
    }
}
