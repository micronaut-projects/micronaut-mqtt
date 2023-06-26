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
package io.micronaut.mqtt.hivemq.bind;

import java.util.List;

/**
 * A MQTT message holds the payload and options
 * for message delivery.
 *
 * @author Sven Kobow
 */
public class MqttMessage {

    private boolean mutable = true;
    private byte[] payload;
    private int qos = 1;
    private boolean retained = false;
    private boolean dup = false;
    private int messageId;
    private byte[] correlationData;
    private List<UserProperty> userProperties;

    public MqttMessage() {
        setPayload(new byte[]{});
    }

    public MqttMessage(final byte[] payload) {
        setPayload(payload);
    }

    /**
     * Returns whether the message is mutable.
     * @return mutable
     */
    public boolean getMutable() {
        return mutable;
    }

    /**
     * Sets whether the message is mutable.
     * @param mutable boolean value
     */
    public void setMutable(final boolean mutable) {
        this.mutable = mutable;
    }

    /**
     * Returns the payload as byte array.
     * @return payload The payload byte array
     */
    public byte[] getPayload() {
        return payload;
    }

    /**
     * Sets the payload of the message.
     * @param payload The payload as byte array
     */
    public void setPayload(final byte[] payload) {
        this.payload = payload;
    }

    /**
     * Returns the quality of service level for the message.
     * @return MQTT quality of service level
     */
    public int getQos() {
        return qos;
    }

    /**
     * Sets the quality of service level for the message.
     * @param qos MQTT quality of service level
     */
    public void setQos(final int qos) {
        this.qos = qos;
    }

    /**
     * Returns whether the message is a retained message.
     * @return boolean value
     */
    public boolean isRetained() {
        return retained;
    }

    /**
     * Sets whether the message is retained.
     * @param retained boolean value
     */
    public void setRetained(final boolean retained) {
        this.retained = retained;
    }

    /**
     * Returns whether the message is flagged as duplicate.
     * @return boolean value
     */
    public boolean getDup() {
        return dup;
    }

    /**
     * Flags the message as duplicate.
     * @param dup boolean value
     */
    public void setDup(final boolean dup) {
        this.dup = dup;
    }

    /**
     * Returns the message id.
     * @return message id
     */
    public int getId() {
        return messageId;
    }

    /**
     * Sets the message id.
     * @param messageId message id
     */
    public void setId(final int messageId) {
        this.messageId = messageId;
    }

    /**
     * Sets the MQTT user properties.
     * @param userProperties MQTT user properties
     */
    public void setUserProperties(final List<UserProperty> userProperties) {
        this.userProperties = userProperties;
    }

    /**
     * Returns the MQTT user properties.
     * @return MQTT user properties
     */
    public List<UserProperty> getUserProperties() {
        return this.userProperties;
    }
}
