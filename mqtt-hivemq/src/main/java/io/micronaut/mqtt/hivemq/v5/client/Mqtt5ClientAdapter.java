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

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.datatypes.MqttTopicFilter;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe;
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscription;
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.Mqtt5Unsubscribe;
import io.micronaut.context.annotation.Requires;
import io.micronaut.mqtt.bind.MqttBindingContext;
import io.micronaut.mqtt.exception.MqttSubscriberException;
import io.micronaut.mqtt.hivemq.bind.MqttMessage;
import io.micronaut.mqtt.hivemq.bind.UserProperty;
import io.micronaut.mqtt.hivemq.client.MqttClientAdapter;
import io.micronaut.mqtt.hivemq.v5.bind.MqttV5BindingContext;
import io.micronaut.mqtt.hivemq.v5.config.Mqtt5ClientConfigurationProperties;
import jakarta.inject.Singleton;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Adapter class for MQTT 3 HiveMQ async client.
 *
 * @author Sven Kobow
 * @since 3.0.0
 * @see MqttClientAdapter
 * @see Mqtt5AsyncClient
 */
@Singleton
@Requires(property = "mqtt.client.mqtt-version", value = "5", defaultValue = "5")
public class Mqtt5ClientAdapter implements MqttClientAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(Mqtt5ClientAdapter.class);

    private final Mqtt5AsyncClient client;
    private final Mqtt5ClientConfigurationProperties configurationProperties;

    public Mqtt5ClientAdapter(@NotNull final Mqtt5AsyncClient client, @NotNull final Mqtt5ClientConfigurationProperties configurationProperties) {
        this.client = client;
        this.configurationProperties = configurationProperties;
    }

    @Override
    public void subscribe(String[] topics, int[] qos, Consumer<MqttBindingContext<MqttMessage>> callback) {

        final Map<String, Integer> topicMap = getTopicMap(topics, qos);

        final Mqtt5Subscribe mqttSubscribe = Mqtt5Subscribe.builder()
            .addSubscriptions(
                topicMap.entrySet().stream().map(topicEntry -> Mqtt5Subscription.builder()
                    .topicFilter(topicEntry.getKey())
                    .qos(Objects.requireNonNull(MqttQos.fromCode(topicEntry.getValue())))
                    .build())
            ).build();

        client.subscribe(mqttSubscribe, mqtt5Publish -> {
                LOG.trace("Received message: {}", new String(mqtt5Publish.getPayloadAsBytes()));

                final MqttMessage mqttMessage = new MqttMessage(mqtt5Publish.getPayloadAsBytes());
                mqttMessage.setQos(mqtt5Publish.getQos().getCode());

                final List<UserProperty> userProperties = mqtt5Publish.getUserProperties()
                    .asList()
                    .stream()
                    .map((prop) -> new UserProperty(prop.getName().toString(), prop.getValue().toString()))
                    .toList();
                mqttMessage.setUserProperties(userProperties);

                mqtt5Publish.getCorrelationData().ifPresent(byteBuffer -> {
                    if (byteBuffer.hasArray() && !byteBuffer.isReadOnly()) {
                        mqttMessage.setCorrelationData(byteBuffer.array());
                        return;
                    }
                    byte[] correlationData = new byte[byteBuffer.capacity()];
                    byteBuffer.get(correlationData);
                    mqttMessage.setCorrelationData(correlationData);
                });

                mqtt5Publish.getContentType().ifPresent(mqttUtf8String -> mqttMessage.setContentType(mqttUtf8String.toString()));
                mqtt5Publish.getPayloadFormatIndicator().ifPresent(mqtt5PayloadFormatIndicator ->
                    mqttMessage.setPayloadFormatIndicator(mqtt5PayloadFormatIndicator.getCode()));
                mqtt5Publish.getMessageExpiryInterval().ifPresent(mqttMessage::setMessageExpiryInterval);
                mqtt5Publish.getResponseTopic().ifPresent(mqttTopic -> mqttMessage.setResponseTopic(mqttTopic.toString()));

                final MqttV5BindingContext context = new MqttV5BindingContext(client, mqttMessage);
                context.setTopic(mqtt5Publish.getTopic().toString());
                context.setMqtt5Publish(mqtt5Publish);

                context.setManualAcks(configurationProperties.getManualAcks());
                callback.accept(context);

            }, configurationProperties.getManualAcks())
            .whenComplete((mqtt5SubAck, throwable) -> {
                if (throwable != null) {
                    throw new MqttSubscriberException(String.format("Failed to subscribe to the topics: %s", throwable.getMessage()), throwable);
                }
            });
    }

    @Override
    public void unsubscribe(Set<String> topics) {
        final Mqtt5Unsubscribe mqtt5Unsubscribe = Mqtt5Unsubscribe.builder()
            .addTopicFilters(
                topics.stream().map(MqttTopicFilter::of)
            ).build();

        client.unsubscribe(mqtt5Unsubscribe);
    }

    @Override
    public boolean isConnected() {
        return client.getState().isConnected();
    }

    @Override
    public Object getClientIdentifier() {
        return client.getConfig().getClientIdentifier().map(Object::toString).orElse(null);
    }
}
