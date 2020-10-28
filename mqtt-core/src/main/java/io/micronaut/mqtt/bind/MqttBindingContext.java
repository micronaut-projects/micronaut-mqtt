package io.micronaut.mqtt.bind;

import io.micronaut.core.annotation.Introspected;

@Introspected
public interface MqttBindingContext<T> {

    byte[] getPayload();

    void setPayload(byte[] payload);

    boolean isRetained();

    void setRetained(boolean retained);

    int getQos();

    void setQos(int qos);

    String getTopic();

    void setTopic(String topic);

    int getId();

    T getNativeMessage();
}
