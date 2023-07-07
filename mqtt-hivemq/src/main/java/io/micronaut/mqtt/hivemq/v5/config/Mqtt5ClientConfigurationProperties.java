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
package io.micronaut.mqtt.hivemq.v5.config;

import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnectRestrictions;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.mqtt.hivemq.config.MqttClientConfigurationProperties;
import io.micronaut.mqtt.ssl.MqttCertificateConfiguration;

import java.util.Map;

/**
 * Configuration for the MQTT v5 client.
 *
 * @author Sven Kobow
 * @since 3.0.0
 * @see MqttClientConfigurationProperties
 */
@ConfigurationProperties(value = "mqtt.client")
public class Mqtt5ClientConfigurationProperties extends MqttClientConfigurationProperties {

    private boolean cleanStart = Mqtt5Connect.DEFAULT_CLEAN_START;
    private Long sessionExpiryInterval = Mqtt5Connect.DEFAULT_SESSION_EXPIRY_INTERVAL;
    private Integer receiveMaximum = Mqtt5ConnectRestrictions.DEFAULT_RECEIVE_MAXIMUM;
    private Integer maximumPacketSize = Mqtt5ConnectRestrictions.DEFAULT_MAXIMUM_PACKET_SIZE;
    private Integer topicAliasMaximum = Mqtt5ConnectRestrictions.DEFAULT_TOPIC_ALIAS_MAXIMUM;
    private boolean requestResponseInfo = Mqtt5ConnectRestrictions.DEFAULT_REQUEST_RESPONSE_INFORMATION;
    private boolean requestProblemInfo = Mqtt5ConnectRestrictions.DEFAULT_REQUEST_PROBLEM_INFORMATION;
    private Map<String, String> userProperties;

    /**
     *
     * @param willMessage an optional last will message
     * @param certificateConfiguration certification configuration for using SSL encrypted connections am mTLS
     */
    public Mqtt5ClientConfigurationProperties(
        @Nullable final WillMessage willMessage, @Nullable final MqttCertificateConfiguration certificateConfiguration) {
        super(willMessage, certificateConfiguration);
    }

    /**
     * @return true if a new sessions should be started for connection.
     */
    public boolean isCleanStart() {
        return cleanStart;
    }

    /**
     * @param cleanStart if connection should start a new session.
     */
    public void setCleanStart(boolean cleanStart) {
        this.cleanStart = cleanStart;
    }

    /**
     * @return the session expiry interval in seconds.
     */
    public Long getSessionExpiryInterval() {
        return sessionExpiryInterval;
    }

    /**
     * @param sessionExpiryInterval the session expiry interval in seconds.
     */
    public void setSessionExpiryInterval(Long sessionExpiryInterval) {
        this.sessionExpiryInterval = sessionExpiryInterval;
    }

    /**
     * @return the maximum amount of not acknowledged publishes with QoS 1 or 2 the client accepts from the server concurrently.
     */
    public Integer getReceiveMaximum() {
        return receiveMaximum;
    }

    /**
     * @param receiveMaximum the maximum amount of not acknowledged publishes with QoS 1 or 2 the client accepts from the server concurrently.
     */
    public void setReceiveMaximum(Integer receiveMaximum) {
        this.receiveMaximum = receiveMaximum;
    }

    /**
     * @return the maximum packet size the client sends to the server.
     */
    public Integer getMaximumPacketSize() {
        return maximumPacketSize;
    }

    /**
     * @param maximumPacketSize the maximum packet size the client sends to the server.
     */
    public void setMaximumPacketSize(Integer maximumPacketSize) {
        this.maximumPacketSize = maximumPacketSize;
    }

    /**
     * @return the maximum amount of topic aliases the client accepts from the server.
     */
    public Integer getTopicAliasMaximum() {
        return topicAliasMaximum;
    }

    /**
     * @param topicAliasMaximum the maximum amount of topic aliases the client accepts from the server.
     */
    public void setTopicAliasMaximum(Integer topicAliasMaximum) {
        this.topicAliasMaximum = topicAliasMaximum;
    }

    /**
     * @return whether the client requests response information from the server.
     */
    public boolean isRequestResponseInfo() {
        return requestResponseInfo;
    }

    /**
     * @param requestResponseInfo whether the client requests response information from the server.
     */
    public void setRequestResponseInfo(boolean requestResponseInfo) {
        this.requestResponseInfo = requestResponseInfo;
    }

    /**
     * @return whether the client requests problem information from the server.
     */
    public boolean isRequestProblemInfo() {
        return requestProblemInfo;
    }

    /**
     * @param requestProblemInfo whether the client requests problem information from the server.
     */
    public void setRequestProblemInfo(boolean requestProblemInfo) {
        this.requestProblemInfo = requestProblemInfo;
    }

    /**
     * @return the user defined properties tha should be sent for every message.
     */
    public Map<String, String> getUserProperties() {
        return userProperties;
    }

    /**
     * @param userProperties the user defined properties tha should be sent for every message.
     */
    public void setUserProperties(Map<String, String> userProperties) {
        this.userProperties = userProperties;
    }
}
