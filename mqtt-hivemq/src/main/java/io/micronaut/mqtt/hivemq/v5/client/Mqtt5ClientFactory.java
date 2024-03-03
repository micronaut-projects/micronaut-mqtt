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
package io.micronaut.mqtt.hivemq.v5.client;

import com.hivemq.client.mqtt.*;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.lifecycle.MqttClientAutoReconnect;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientBuilder;
import com.hivemq.client.mqtt.mqtt5.auth.Mqtt5EnhancedAuthMechanism;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties;
import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserPropertiesBuilder;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnectBuilder;
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnectRestrictions;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.exceptions.BeanInstantiationException;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.StringUtils;
import io.micronaut.mqtt.exception.MqttClientException;
import io.micronaut.mqtt.hivemq.client.MqttClientFactory;
import io.micronaut.mqtt.hivemq.ssl.KeyManagerFactoryCreationException;
import io.micronaut.mqtt.hivemq.ssl.TrustManagerFactoryCreationException;
import io.micronaut.mqtt.hivemq.v5.config.Mqtt5ClientConfiguration;
import io.micronaut.mqtt.ssl.MqttCertificateConfiguration;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * A factory to create an MQTT v3 client.
 *
 * @author Sven Kobow
 * @since 3.0.0
 */
@Factory
@Requires(property = "mqtt.client.mqtt-version", value = "5", defaultValue = "5")
public final class Mqtt5ClientFactory implements MqttClientFactory {

    private static final Logger LOG = LoggerFactory.getLogger(Mqtt5ClientFactory.class);

    /**
     * Creates a new instance of a {@link Mqtt5AsyncClient} with the given configuration.
     * @param configuration The configuration to apply.
     * @param enhancedAuthMechanism An optional implementation of {@link Mqtt5EnhancedAuthMechanism} to add enhanced authentication.
     * @return A new instance of {@link Mqtt5AsyncClient}
     */
    @Singleton
    @Bean(preDestroy = "disconnect")
    Mqtt5AsyncClient mqttClient(final Mqtt5ClientConfiguration configuration, @Nullable final Mqtt5EnhancedAuthMechanism enhancedAuthMechanism) {

        final Mqtt5ClientBuilder clientBuilder = MqttClient.builder()
            .useMqttVersion5()
            .identifier(configuration.getClientId())
            .transportConfig(buildTransportConfig(configuration));

        if (configuration.isAutomaticReconnect()) {
            clientBuilder.automaticReconnect()
                .initialDelay(MqttClientAutoReconnect.DEFAULT_START_DELAY_S, TimeUnit.SECONDS)
                .maxDelay(configuration.getMaxReconnectDelay(), TimeUnit.SECONDS)
                .applyAutomaticReconnect();
        }

        final Mqtt5ConnectBuilder connectBuilder = Mqtt5Connect.builder()
            .cleanStart(configuration.isCleanStart())
            .keepAlive(configuration.getKeepAliveInterval())
            .sessionExpiryInterval(configuration.getSessionExpiryInterval())
            .restrictions(Mqtt5ConnectRestrictions.builder()
                .receiveMaximum(configuration.getReceiveMaximum())
                .maximumPacketSize(configuration.getMaximumPacketSize())
                .topicAliasMaximum(configuration.getTopicAliasMaximum())
                .requestResponseInformation(configuration.isRequestResponseInfo())
                .requestProblemInformation(configuration.isRequestProblemInfo())
                .build());

        if (configuration.getUserProperties() != null && configuration.getUserProperties().size() > 0) {
            connectBuilder.userProperties(buildUserProperties(configuration));
        }

        if (enhancedAuthMechanism != null) {
            connectBuilder.enhancedAuth(enhancedAuthMechanism);
        }

        if (StringUtils.isNotEmpty(configuration.getUserName())) {
            connectBuilder.simpleAuth()
                .username(configuration.getUserName())
                .password(configuration.getPassword())
                .applySimpleAuth();
        }

        if (configuration.getWillMessage() != null) {
            var willMessage = configuration.getWillMessage();

            connectBuilder.willPublish()
                .topic(willMessage.getTopic())
                .payload(willMessage.getPayload())
                .qos(Objects.requireNonNull(MqttQos.fromCode(willMessage.getQos())))
                .retain(willMessage.isRetained());
        }

        final var client = clientBuilder.buildAsync();

        if (LOG.isTraceEnabled()) {
            LOG.trace("Connecting to {} on port {}", configuration.getServerHost(), configuration.getServerPort());
        }

        client.connect(connectBuilder.build())
            .whenComplete((mqtt3ConnAck, throwable) -> {
                if (throwable != null) {
                    throw new MqttClientException("Error connecting mqtt client");
                }
            }).join();

        return client;
    }

    private Mqtt5UserProperties buildUserProperties(final Mqtt5ClientConfiguration configuration) {
        final Mqtt5UserPropertiesBuilder propertiesBuilder = Mqtt5UserProperties.builder();
        configuration.getUserProperties().forEach(propertiesBuilder::add);

        return propertiesBuilder.build();
    }

    private MqttClientTransportConfig buildTransportConfig(final Mqtt5ClientConfiguration configuration) {

        final MqttClientTransportConfigBuilder transportConfigBuilder = MqttClientTransportConfig.builder()
            .serverHost(configuration.getServerHost())
            .serverPort(configuration.getServerPort())
            .mqttConnectTimeout(configuration.getConnectionTimeout().toMillis(), TimeUnit.MILLISECONDS);

        if (configuration.isSSL()) {
            if (configuration.getCertificateConfiguration() != null) {
                final MqttCertificateConfiguration certConfiguration = configuration.getCertificateConfiguration();
                final MqttClientSslConfigBuilder sslConfigBuilder = MqttClientSslConfig.builder();
                if (configuration.isHttpsHostnameVerificationEnabled()) {
                    sslConfigBuilder.hostnameVerifier(configuration.getSSLHostnameVerifier());
                }

                try {
                    sslConfigBuilder
                        .keyManagerFactory(getKeyManagerFactory(certConfiguration))
                        .trustManagerFactory(getTrustManagerFactory(certConfiguration));

                } catch (KeyManagerFactoryCreationException | TrustManagerFactoryCreationException e) {
                    throw new BeanInstantiationException("Error creating SSL configuration", e);
                }

                if (configuration.isHttpsHostnameVerificationEnabled()) {
                    sslConfigBuilder.hostnameVerifier(configuration.getSSLHostnameVerifier());
                }

                transportConfigBuilder.sslConfig(sslConfigBuilder.build());
            } else {
                transportConfigBuilder.sslWithDefaultConfig();
            }
        }

        return transportConfigBuilder.build();
    }
}
