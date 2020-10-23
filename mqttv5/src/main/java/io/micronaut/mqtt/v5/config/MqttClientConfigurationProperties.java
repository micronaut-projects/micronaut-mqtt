package io.micronaut.mqtt.v5.config;

import edu.umd.cs.findbugs.annotations.Nullable;
import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.ConfigurationProperties;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;

import javax.net.ssl.HostnameVerifier;

@ConfigurationProperties("mqtt.client")
public class MqttClientConfigurationProperties  {

    private String serverUri;
    private String clientId;

    @ConfigurationBuilder
    private final MqttConnectionOptions connectOptions = new MqttConnectionOptions();

    public MqttClientConfigurationProperties(@Nullable HostnameVerifier hostnameVerifier,
                                             WillMessage willMessage) {
        connectOptions.setSSLHostnameVerifier(hostnameVerifier);
        if (willMessage.getTopic() != null) {
            connectOptions.setWill(willMessage.getTopic(), new MqttMessage(willMessage.getPayload(), willMessage.getQos(), willMessage.isRetained(), willMessage.getProperties()));
        }
    }

    public MqttConnectionOptions getConnectOptions() {
        return connectOptions;
    }

    public String getServerUri() {
        return serverUri;
    }

    public void setServerUri(String serverUri) {
        this.serverUri = serverUri;
    }

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

        @ConfigurationBuilder("properties")
        private MqttProperties properties = new MqttProperties();

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

        public MqttProperties getProperties() {
            return properties;
        }

        public void setProperties(MqttProperties properties) {
            this.properties = properties;
        }
    }
}
