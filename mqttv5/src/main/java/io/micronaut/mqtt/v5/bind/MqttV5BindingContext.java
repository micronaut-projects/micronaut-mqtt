package io.micronaut.mqtt.v5.bind;


import io.micronaut.mqtt.bind.MqttBindingContext;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;

public class MqttV5BindingContext implements MqttBindingContext<MqttMessage> {

    private final MqttMessage message;
    private String topic;

    public MqttV5BindingContext(MqttMessage message) {
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

    public MqttProperties getProperties() {
        return message.getProperties();
    }

    public void setProperties(MqttProperties properties) {
        message.setProperties(properties);
    }

    @Override
    public int getId() {
        return message.getId();
    }

    @Override
    public MqttMessage getNativeMessage() {
        return message;
    }
}
