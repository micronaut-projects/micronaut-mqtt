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
package io.micronaut.mqtt.hivemq.config;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttClientTransportConfig;
import com.hivemq.client.mqtt.lifecycle.MqttClientAutoReconnect;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.mqtt.ssl.MqttCertificateConfiguration;

import javax.net.ssl.HostnameVerifier;
import java.net.URI;
import java.time.Duration;
import java.util.Map;

/**
 * Abstract base class for MQTT client configuration.
 *
 * @author Sven Kobow
 */
public abstract class MqttClientConfigurationProperties {
    private URI serverUri = URI.create(String.format("tcp:/%s:%s", MqttClient.DEFAULT_SERVER_HOST, MqttClient.DEFAULT_SERVER_PORT));
    private String clientId = null;
    private Duration connectionTimeout = Duration.ofSeconds(MqttClientTransportConfig.DEFAULT_MQTT_CONNECT_TIMEOUT_MS);
    private Boolean manualAcks = false;
    private byte[] password = null;
    private String userName = null;
    private Integer keepAliveInterval = Mqtt5Connect.DEFAULT_KEEP_ALIVE;
    private Long maxReconnectDelay = MqttClientAutoReconnect.DEFAULT_MAX_DELAY_S;
    private boolean automaticReconnect = false;
    private Map<String, String> customWebSocketHeaders = null;
    private boolean isHttpsHostnameVerificationEnabled = false;
    private HostnameVerifier sslHostnameVerifier = null;
    private WillMessage willMessage = null;
    private final MqttCertificateConfiguration certificateConfiguration;

    /**
     * @param willMessage an optional last will message.
     * @param certificateConfiguration certification configuration for using SSL encrypted connections and mTLS.
     */
    protected MqttClientConfigurationProperties(final WillMessage willMessage, final MqttCertificateConfiguration certificateConfiguration) {
        if (willMessage.getTopic() != null) {
            this.willMessage = willMessage;
        }
        this.certificateConfiguration = certificateConfiguration;
    }

    /**
     * @return the uri of server to connect to as [schema]://[serverHost]:[serverPort].
     */
    public URI getServerUri() {
        return this.serverUri;
    }

    /**
     * @param serverUri the uri of server to connect to as [schema]://[serverHost]:[serverPort].
     */
    public void setServerUri(@Nullable final URI serverUri) {
        this.serverUri = serverUri;
    }

    /**
     * @return the server host of the configured {@link #serverUri}.
     */
    public String getServerHost() {
        return serverUri != null ? serverUri.getHost() : null;
    }

    /**
     * @return the server port of the configured {@link #serverUri}.
     */
    public Integer getServerPort() {
        return serverUri != null ? serverUri.getPort() : null;
    }

    /**
     * @return true if the schema of the configured {@link #serverUri} is ssl.
     */
    public boolean isSSL() {
        return serverUri != null && "SSL".equalsIgnoreCase(serverUri.getScheme());
    }

    /**
     * @return the client identifier.
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * @param clientId the client identifier.
     */
    public void setClientId(final String clientId) {
        this.clientId = clientId;
    }

    /**
     * @return the connection timeout.
     */
    public Duration getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * @param connectionTimeout how long to wait for a connection.
     */
    public void setConnectionTimeout(final Duration connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    /**
     * @return an optional boolean to set the client in manual acknowledge mode.
     */
    public boolean getManualAcks() {
        return this.manualAcks;
    }

    /**
     * @param manualAcks set to true if you wish to manually acknowledge messages.
     */
    public void setManualAcks(final boolean manualAcks) {
        this.manualAcks = manualAcks;
    }

    /**
     * @return the password to use for MQTT connections.
     */
    public byte[] getPassword() {
        return password;
    }

    /**
     * @param password the password to use for MQTT connections.
     */
    public void setPassword(@Nullable final byte[] password) {
        this.password = password;
    }

    /**
     * @return the username to use for MQTT connections.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @param userName the username to use for MQTT connections.
     */
    public void setUserName(@Nullable final String userName) {
        this.userName = userName;
    }

    /**
     * @return the maximal delay for reconnecting.
     */
    public Long getMaxReconnectDelay() {
        return maxReconnectDelay;
    }

    /**
     * @param maxReconnectDelay the maximum delay for reconnecting.
     */
    public void setMaxReconnectDelay(final long maxReconnectDelay) {
        this.maxReconnectDelay = maxReconnectDelay;
    }

    /**
     * @return the keep alive interval.
     */
    public Integer getKeepAliveInterval() {
        return keepAliveInterval;
    }

    /**
     * @param keepAliveInterval the keep alive interval.
     */
    public void setKeepAliveInterval(final int keepAliveInterval) {
        this.keepAliveInterval = keepAliveInterval;
    }

    /**
     * @return true is automatic reconnect should be performed.
     */
    public boolean isAutomaticReconnect() {
        return automaticReconnect;
    }

    /**
     * @param automaticReconnect if an automatic reconnect should be performed.
     */
    public void setAutomaticReconnect(final boolean automaticReconnect) {
        this.automaticReconnect = automaticReconnect;
    }

    /**
     * @return the custom headers that should be sent with web socket connections.
     */
    public Map<String, String> getCustomWebSocketHeaders() {
        return customWebSocketHeaders;
    }

    /**
     * @param customWebSocketHeaders the custom headers that should be sent with web socket connections.
     */
    public void setCustomWebSocketHeaders(@Nullable final Map<String, String> customWebSocketHeaders) {
        this.customWebSocketHeaders = customWebSocketHeaders;
    }

    /**
     * @return true if hostname verification should be used.
     */
    public boolean isHttpsHostnameVerificationEnabled() {
        return this.isHttpsHostnameVerificationEnabled;
    }

    /**
     * @param httpsHostnameVerificationEnabled true if hostname verification should be used.
     */
    public void setHttpsHostnameVerificationEnabled(boolean httpsHostnameVerificationEnabled) {
        this.isHttpsHostnameVerificationEnabled = httpsHostnameVerificationEnabled;
    }

    /**
     * @return the hostname verifier to use for hostname verification.
     */
    public HostnameVerifier getSSLHostnameVerifier() {
        return sslHostnameVerifier;
    }

    /**
     * @param hostnameVerifier the hostname verifier to use for hostname verification.
     */
    public void setSSLHostnameVerifier(@Nullable final HostnameVerifier hostnameVerifier) {
        this.sslHostnameVerifier = hostnameVerifier;
    }

    /**
     * @return the last will message that should be sent on ungraceful disconnects.
     */
    public WillMessage getWillMessage() {
        return willMessage;
    }

    /**
     * @return the certificate configuration to use for SSL and mTLS.
     */
    public MqttCertificateConfiguration getCertificateConfiguration() {
        return certificateConfiguration;
    }

    /**
     * Configuration for a last will message.
     */
    @ConfigurationProperties("will-message")
    public static class WillMessage {

        private String topic;
        private byte[] payload;
        private int qos;
        private boolean retained;

        /**
         * @return The topic to publish to
         */
        public String getTopic() {
            return topic;
        }

        /**
         * @param topic The topic to publish to
         */
        public void setTopic(String topic) {
            this.topic = topic;
        }

        /**
         * @return The message payload
         */
        public byte[] getPayload() {
            return payload;
        }

        /**
         * @param payload The message payload
         */
        public void setPayload(byte[] payload) {
            this.payload = payload;
        }

        /**
         * @return The message qos
         */
        public int getQos() {
            return qos;
        }

        /**
         * @param qos The message qos
         */
        public void setQos(int qos) {
            this.qos = qos;
        }

        /**
         * @return True if the message should be retained
         */
        public boolean isRetained() {
            return retained;
        }

        /**
         * @param retained If the message should be retained
         */
        public void setRetained(boolean retained) {
            this.retained = retained;
        }
    }
}
