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

import com.hivemq.client.internal.mqtt.message.publish.MqttPublish;

import java.util.List;

/**
 * A MQTT message holds the payload and options
 * for message delivery.
 *
 * @author Sven Kobow
 * @since 3.0.0
 */
public class MqttMessage {

    private int messageId;
    private byte[] payload;
    private int payloadFormatIndicator = 0;
    private int qos = 1;
    private boolean retained = false;
    private byte[] correlationData;
    private String contentType;
    private List<UserProperty> userProperties;
    private long messageExpiryInterval = MqttPublish.NO_MESSAGE_EXPIRY;
    private String responseTopic = null;

    public MqttMessage() {
        setPayload(new byte[]{});
    }

    public MqttMessage(final byte[] payload) {
        setPayload(payload);
    }

    /**
     * Returns the payload as byte array.
     * @return payload The payload byte array.
     */
    public byte[] getPayload() {
        return payload;
    }

    /**
     * Sets the payload of the message.
     * @param payload The payload as byte array.
     */
    public void setPayload(final byte[] payload) {
        this.payload = payload;
    }

    /**
     * Returns the payload format indicator.
     * 0 = unspecified byte stream
     * 1 = UTF-8 encoded payload
     * @return The MQTT payload indicator.
     */
    public int getPayloadFormatIndicator() {
        return payloadFormatIndicator;
    }

    /**
     * Sets the payload format indicator.
     * @param payloadFormatIndicator Use 0 for unspecified byte stream and 1 for UTF-8 encoded payload.
     */
    public void setPayloadFormatIndicator(int payloadFormatIndicator) {
        this.payloadFormatIndicator = payloadFormatIndicator;
    }

    /**
     * Returns the quality of service level for the message.
     * @return MQTT quality of service level.
     */
    public int getQos() {
        return qos;
    }

    /**
     * Sets the quality of service level for the message.
     * @param qos MQTT quality of service level.
     */
    public void setQos(final int qos) {
        this.qos = qos;
    }

    /**
     * Returns whether the message is a retained message.
     * @return boolean value.
     */
    public boolean isRetained() {
        return retained;
    }

    /**
     * Sets whether the message is retained.
     * @param retained boolean value.
     */
    public void setRetained(final boolean retained) {
        this.retained = retained;
    }

    /**
     * Returns the message id.
     * @return message id.
     */
    public int getId() {
        return messageId;
    }

    /**
     * Sets the message id.
     * @param messageId message id.
     */
    public void setId(final int messageId) {
        this.messageId = messageId;
    }

    /**
     * Sets the MQTT user properties.
     * @param userProperties MQTT user properties.
     */
    public void setUserProperties(final List<UserProperty> userProperties) {
        this.userProperties = userProperties;
    }

    /**
     * Returns the MQTT user properties.
     * @return MQTT user properties.
     */
    public List<UserProperty> getUserProperties() {
        return this.userProperties;
    }

    /**
     * Returns the correlation data for the message.
     * @return the correlation data.
     */
    public byte[] getCorrelationData() {
        return correlationData;
    }

    /**
     * Sets the correlation data for the message.
     * @param correlationData the correlation data.
     */
    public void setCorrelationData(byte[] correlationData) {
        this.correlationData = correlationData;
    }

    /**
     * Returns a string describing the content type.
     * @see #payloadFormatIndicator
     * @return A string describing the content type.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Sets the content type description for the message.
     * @param contentType A string describing the content type.
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Returns the message expiry interval in seconds.
     * @return The message expiry interval in seconds.
     */
    public long getMessageExpiryInterval() {
        return messageExpiryInterval;
    }

    /**
     * Sets the message expiry interval in seconds.
     * @param messageExpiryInterval The message expiry interval in seconds.
     */
    public void setMessageExpiryInterval(long messageExpiryInterval) {
        this.messageExpiryInterval = messageExpiryInterval;
    }

    /**
     * Returns the response topic for using MQTT v5 request response pattern.
     * @return The response topic.
     */
    public String getResponseTopic() {
        return responseTopic;
    }

    /**
     * Sets the response topic for using MQTT v5 request response pattern.
     * @param responseTopic The reponse topic.
     */
    public void setResponseTopic(String responseTopic) {
        this.responseTopic = responseTopic;
    }
}
