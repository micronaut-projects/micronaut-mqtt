package io.micronaut.mqtt.v5.bind;


import org.eclipse.paho.mqttv5.common.MqttMessage;

public class MqttV5Message implements io.micronaut.mqtt.bind.MqttMessage<MqttMessage> {

    private final MqttMessage message;
    private String topic;

    public MqttV5Message(MqttMessage message) {
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

    @Override
    public MqttMessage getNativeMessage() {
        return message;
    }
}
