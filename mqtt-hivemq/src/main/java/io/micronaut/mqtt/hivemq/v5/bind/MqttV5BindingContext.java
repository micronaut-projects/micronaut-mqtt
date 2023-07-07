/*
 * Copyright 2017-2023 original authors
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
package io.micronaut.mqtt.hivemq.v5.bind;

import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import io.micronaut.core.annotation.Internal;
import io.micronaut.mqtt.bind.MqttBindingContext;
import io.micronaut.mqtt.hivemq.bind.MqttMessage;

/**
 * A binding context for MQTT v5 messages.
 *
 * @author Sven Kobow
 * @since 3.0.0
 */
@Internal
public final class MqttV5BindingContext implements MqttBindingContext<MqttMessage> {

    private final Mqtt5AsyncClient client;
    private final MqttMessage message;
    private String topic;
    private boolean manualAcks = false;
    private Mqtt5Publish mqtt5Publish;

    /**
     * @param client The client
     * @param message The message
     */
    public MqttV5BindingContext(Mqtt5AsyncClient client, MqttMessage message) {
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

    @Override
    public int getId() {
        return message.getId();
    }

    @Override
    public void acknowlege() {
        if (mqtt5Publish != null && manualAcks) {
            mqtt5Publish.acknowledge();
        }
    }

    @Override
    public MqttMessage getNativeMessage() {
        return message;
    }

    /**
     * @param manualAcks If messages should be acknowledged manually
     */
    public void setManualAcks(final boolean manualAcks) {
        this.manualAcks = manualAcks;
    }

    /**
     * @param mqtt5Publish The raw MQTT v5 publish paket
     */
    public void setMqtt5Publish(final Mqtt5Publish mqtt5Publish) {
        this.mqtt5Publish = mqtt5Publish;
    }
}
