package io.micronaut.mqtt.v3.config;

import edu.umd.cs.findbugs.annotations.Nullable;
import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Context;
import org.eclipse.paho.client.mqttv3.*;

import javax.net.ssl.HostnameVerifier;
import javax.validation.constraints.NotNull;

@ConfigurationProperties("mqtt.client")
@Context
public class MqttClientConfigurationProperties  {

    private String serverUri;
    private String clientId;

    @ConfigurationBuilder()
    private final MqttConnectOptions connectOptions = new MqttConnectOptions();

    public MqttClientConfigurationProperties(@Nullable HostnameVerifier hostnameVerifier,
                                             WillMessage willMessage) {
        connectOptions.setSSLHostnameVerifier(hostnameVerifier);
        if (willMessage.getTopic() != null) {
            connectOptions.setWill(willMessage.getTopic(), willMessage.getPayload(), willMessage.getQos(), willMessage.isRetained());
        }
    }

    public MqttConnectOptions getConnectOptions() {
        return connectOptions;
    }

    @NotNull
    public String getServerUri() {
        return serverUri;
    }

    public void setServerUri(String serverUri) {
        this.serverUri = serverUri;
    }

    @NotNull
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @ConfigurationProperties("will-message")
    public static class WillMessage {

        private String topic;
        private byte[] payload;
        private int qos;
        private boolean retained;

        public String getTopic() {
            return topic;
        }

        public void setTopic(String topic) {
            this.topic = topic;
        }

        public byte[] getPayload() {
            return payload;
        }

        public void setPayload(byte[] payload) {
            this.payload = payload;
        }

        public int getQos() {
            return qos;
        }

        public void setQos(int qos) {
            this.qos = qos;
        }

        public boolean isRetained() {
            return retained;
        }

        public void setRetained(boolean retained) {
            this.retained = retained;
        }
    }
}
