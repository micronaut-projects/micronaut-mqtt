package io.micronaut.mqtt.bind;

public interface MqttMessage<T> {

    byte[] getPayload();

    void setPayload(byte[] payload);

    boolean isRetained();

    void setRetained(boolean retained);

    int getQos();

    void setQos(int qos);

    String getTopic();

    void setTopic(String topic);

    T getNativeMessage();
}
