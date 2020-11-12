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
package io.micronaut.mqtt.v5.config;

import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.mqtt.config.MqttSSLConfiguration;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;

import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.util.Properties;

/**
 * Configuration for the MQTT client.
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@ConfigurationProperties("mqtt.client")
public class MqttClientConfigurationProperties implements MqttSSLConfiguration {

    private String serverUri;
    private String clientId;
    private Duration connectionTimeout = Duration.ofSeconds(3);

    @ConfigurationBuilder(excludes = {"socketFactory", "SSLProperties", "httpsHostnameVerificationEnabled", "SSLHostnameVerifier"})
    private final MqttConnectionOptions connectOptions = new MqttConnectionOptions();

    public MqttClientConfigurationProperties(WillMessage willMessage) {
        if (willMessage.getTopic() != null) {
            connectOptions.setWill(willMessage.getTopic(), new MqttMessage(willMessage.getPayload(), willMessage.getQos(), willMessage.isRetained(), willMessage.getProperties()));
        }
    }

    /**
     * @return The connection options
     */
    public MqttConnectionOptions getConnectOptions() {
        return connectOptions;
    }

    /**
     * @return The server URI
     */
    @NotNull
    public String getServerUri() {
        return serverUri;
    }

    /**
     * @param serverUri The server URI
     */
    public void setServerUri(String serverUri) {
        this.serverUri = serverUri;
    }

    /**
     * @return The client id
     */
    @NotNull
    public String getClientId() {
        return clientId;
    }

    /**
     * @param clientId The client ID
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * @return The connection timeout
     */
    public Duration getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * @param connectionTimeout How long to wait for a connection
     */
    public void setConnectionTimeout(Duration connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    @Override
    public SocketFactory getSocketFactory() {
        return connectOptions.getSocketFactory();
    }

    @Override
    public void setSocketFactory(SocketFactory socketFactory) {
        connectOptions.setSocketFactory(socketFactory);
    }

    @Override
    public Properties getSSLProperties() {
        return connectOptions.getSSLProperties();
    }

    @Override
    public void setSSLProperties(Properties props) {
        connectOptions.setSSLProperties(props);
    }

    @Override
    public boolean isHttpsHostnameVerificationEnabled() {
        return connectOptions.isHttpsHostnameVerificationEnabled();
    }

    @Override
    public void setHttpsHostnameVerificationEnabled(boolean httpsHostnameVerificationEnabled) {
        connectOptions.setHttpsHostnameVerificationEnabled(httpsHostnameVerificationEnabled);
    }

    @Override
    public HostnameVerifier getSSLHostnameVerifier() {
        return connectOptions.getSSLHostnameVerifier();
    }

    @Override
    public void setSSLHostnameVerifier(HostnameVerifier hostnameVerifier) {
        connectOptions.setSSLHostnameVerifier(hostnameVerifier);
    }

    @ConfigurationProperties("will-message")
    static class WillMessage {

        private String topic;
        private byte[] payload;
        private int qos;
        private boolean retained;

        @ConfigurationBuilder("properties")
        private MqttProperties properties = new MqttProperties();

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

        /**
         * @return The message properties
         */
        public MqttProperties getProperties() {
            return properties;
        }

        /**
         * @param properties The message properties
         */
        public void setProperties(MqttProperties properties) {
            this.properties = properties;
        }
    }
}
