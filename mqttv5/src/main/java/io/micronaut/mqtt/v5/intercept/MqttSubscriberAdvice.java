package io.micronaut.mqtt.v5.intercept;

import io.micronaut.context.BeanContext;
import io.micronaut.mqtt.bind.MqttBinderRegistry;
import io.micronaut.mqtt.bind.MqttBindingContext;
import io.micronaut.mqtt.exception.MqttSubscriberException;
import io.micronaut.mqtt.intercept.AbstractMqttSubscriberAdvice;
import io.micronaut.mqtt.v5.bind.MqttV5BindingContext;
import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttAsyncClient;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.MqttSubscription;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Consumer;

@Singleton
public class MqttSubscriberAdvice extends AbstractMqttSubscriberAdvice<MqttMessage> {

    private static final Logger LOG = LoggerFactory.getLogger(MqttSubscriberAdvice.class);
    private final MqttAsyncClient mqttAsyncClient;

    public MqttSubscriberAdvice(BeanContext beanContext,
                                MqttBinderRegistry binderRegistry,
                                MqttAsyncClient mqttAsyncClient) {
        super(beanContext, binderRegistry);
        this.mqttAsyncClient = mqttAsyncClient;
    }

    @Override
    public void subscribe(String topic, int qos, Consumer<MqttBindingContext<MqttMessage>> callback) {
        try {
            //workaround for https://github.com/eclipse/paho.mqtt.java/issues/826
            final MqttProperties props = new MqttProperties();
            props.setSubscriptionIdentifiers(Arrays.asList(new Integer[] { 0 }));

            mqttAsyncClient.subscribe(new MqttSubscription(topic, qos), null, null, (actualTopic, message) -> {
                MqttV5BindingContext context = new MqttV5BindingContext(message);
                context.setTopic(actualTopic);
                callback.accept(context);
            }, props);
        } catch (MqttException e) {
            throw new MqttSubscriberException(String.format("Failed to subscribe to the topic: %s", topic), e);
        }
    }

    @Override
    public void unsubscribe(Set<String> topics) {
        try {
            IMqttToken token = mqttAsyncClient.unsubscribe(topics.toArray(new String[]{}));
            token.waitForCompletion();
        } catch (MqttException e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Failed to unsubscribe from the subscribed topics", e);
            }
        }
    }
}
