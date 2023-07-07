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

import io.micronaut.mqtt.ssl.MqttCertificateConfiguration;

import javax.net.ssl.HostnameVerifier;
import java.net.URI;
import java.time.Duration;
import java.util.Map;

/**
 * MQTT Client Configuration. You will probably want to inject either {@link io.micronaut.mqtt.hivemq.v3.config.Mqtt3ClientConfiguration} or {@link io.micronaut.mqtt.hivemq.v5.config.Mqtt5ClientConfiguration}.
 * @author Sergio del Amo
 * @since 3.0.0
 */
public interface MqttClientConfiguration {
    /**
     * @return the uri of server to connect to as [schema]://[serverHost]:[serverPort].
     */
    URI getServerUri();

    /**
     * @return the server host of the configured server URI.
     */
    String getServerHost();

    /**
     * @return the server port of the configured server URI.
     */
    Integer getServerPort();

    /**
     * @return true if the schema of the configured server URI is ssl.
     */
    boolean isSSL();

    /**
     * @return the client identifier.
     */
    String getClientId();

    /**
     * @return the MQTT version to use.
     */
    int getMqttVersion();

    /**
     * @return the connection timeout.
     */
    Duration getConnectionTimeout();

    /**
     * @return an optional boolean to set the client in manual acknowledge mode.
     */
    boolean getManualAcks();

    /**
     * @return the password to use for MQTT connections.
     */
    byte[] getPassword();

    /**
     * @return the username to use for MQTT connections.
     */
    String getUserName();

    /**
     * @return the maximal delay for reconnecting.
     */
    Long getMaxReconnectDelay();

    /**
     * @return the keep alive interval.
     */
    Integer getKeepAliveInterval();

    /**
     * @return true is automatic reconnect should be performed.
     */
    boolean isAutomaticReconnect();

    /**
     * @return the custom headers that should be sent with web socket connections.
     */
    Map<String, String> getCustomWebSocketHeaders();

    /**
     * @return true if hostname verification should be used.
     */
    boolean isHttpsHostnameVerificationEnabled();

    /**
     * @return the hostname verifier to use for hostname verification.
     */
    HostnameVerifier getSSLHostnameVerifier();

    /**
     * @return the last will message that should be sent on ungraceful disconnects.
     */
    MqttClientConfigurationProperties.WillMessage getWillMessage();

    /**
     * @return the certificate configuration to use for SSL and mTLS.
     */
    MqttCertificateConfiguration getCertificateConfiguration();
}
