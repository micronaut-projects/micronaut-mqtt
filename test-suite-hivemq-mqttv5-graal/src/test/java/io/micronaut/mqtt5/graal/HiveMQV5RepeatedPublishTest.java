package io.micronaut.mqtt5.graal;

import io.micronaut.context.annotation.Prototype;
import io.micronaut.context.annotation.Requires;
import io.micronaut.mqtt.annotation.MqttSubscriber;
import io.micronaut.mqtt.annotation.Topic;
import io.micronaut.mqtt.annotation.v5.MqttPublisher;
import io.micronaut.mqtt.test.MQTT5Test;
import io.micronaut.mqtt.test.intercept.PrototypePublishClient;
import io.micronaut.mqtt.test.intercept.RepeatedPublishClient;
import io.micronaut.mqtt.test.intercept.RepeatedPublishSpec;

class HiveMQV5RepeatedPublishTest extends RepeatedPublishSpec implements MQTT5Test {

    @Override
    public Class<? extends RepeatedPublishClient> getClient() {
        return Client.class;
    }

    @Override
    public Class<? extends PrototypePublishClient> getPrototypeClient() {
        return PrototypeClient.class;
    }

    @Override
    public Class<? extends RepeatedSubscriber> getSubscriber() {
        return Subscriber.class;
    }

    @Requires(property = "spec.name", value = "HiveMQV5RepeatedPublishTest")
    @MqttPublisher
    interface Client extends RepeatedPublishClient {
    }

    @Requires(property = "spec.name", value = "HiveMQV5RepeatedPublishTest")
    @MqttPublisher
    @Prototype
    interface PrototypeClient extends PrototypePublishClient {
    }

    @Requires(property = "spec.name", value = "HiveMQV5RepeatedPublishTest")
    @MqttSubscriber
    static class Subscriber extends RepeatedSubscriber {

        @Topic(TOPIC)
        void get(String payload) {
            receive(payload);
        }
    }
}
