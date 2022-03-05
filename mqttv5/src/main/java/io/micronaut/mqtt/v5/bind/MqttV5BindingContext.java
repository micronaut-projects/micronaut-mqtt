/*
 * Copyright 2017-2022 original authors
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
package io.micronaut.mqtt.v5.bind;

import io.micronaut.core.annotation.Internal;
import io.micronaut.mqtt.bind.MqttBindingContext;
import io.micronaut.mqtt.exception.MqttSubscriberException;
import org.eclipse.paho.mqttv5.client.MqttAsyncClient;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A binding context for MQTT v5 messages.
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@Internal
public final class MqttV5BindingContext implements MqttBindingContext<MqttMessage> {

    private static final Logger LOG = LoggerFactory.getLogger(MqttV5BindingContext.class);

    private final MqttAsyncClient client;
    private final MqttMessage message;
    private String topic;

    /**
     * @param client The client
     * @param message The message
     */
    public MqttV5BindingContext(MqttAsyncClient client, MqttMessage message) {
        this.client = client;
        this.message = message;
    }

    @Override
    public byte[] getPayload() {
        return message.getPayload();
    }

    @Override
    public void setPayload(byte[] payload) {
        message.setPayload(payload);
    }

    @Override
    public boolean isRetained() {
        return message.isRetained();
    }

    @Override
    public void setRetained(boolean retained) {
        message.setRetained(retained);
    }

    @Override
    public int getQos() {
        return message.getQos();
    }

    @Override
    public void setQos(int qos) {
        message.setQos(qos);
    }

    @Override
    public String getTopic() {
        return topic;
    }

    @Override
    public void setTopic(String topic) {
        this.topic = topic;
    }

    public MqttProperties getProperties() {
        return message.getProperties();
    }

    public void setProperties(MqttProperties properties) {
        message.setProperties(properties);
    }

    @Override
    public int getId() {
        return message.getId();
    }

    @Override
    public void acknowlege() {
        try {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Acknowledging message id {} with qos {}", message.getId(), message.getQos());
            }
            client.messageArrivedComplete(message.getId(), message.getQos());
        } catch (MqttException e) {
            throw new MqttSubscriberException("Failed to acknowledge the message", e);
        }
    }

    @Override
    public MqttMessage getNativeMessage() {
        return message;
    }
}
