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
package io.micronaut.mqtt.hivemq.v3.client;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.datatypes.MqttTopicFilter;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3Subscribe;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.Mqtt3Subscription;
import com.hivemq.client.mqtt.mqtt3.message.unsubscribe.Mqtt3Unsubscribe;
import io.micronaut.context.annotation.Requires;
import io.micronaut.mqtt.bind.MqttBindingContext;
import io.micronaut.mqtt.exception.MqttSubscriberException;
import io.micronaut.mqtt.hivemq.bind.MqttMessage;
import io.micronaut.mqtt.hivemq.client.MqttClientAdapter;
import io.micronaut.mqtt.hivemq.v3.bind.MqttV3BindingContext;
import io.micronaut.mqtt.hivemq.v3.config.Mqtt3ClientConfigurationProperties;
import jakarta.inject.Singleton;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * @see Mqtt3AsyncClient
 */
@Singleton
@Requires(property = "mqtt.client.mqtt-version", value = "3")
public final class Mqtt3ClientAdapter implements MqttClientAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(Mqtt3ClientAdapter.class);

    private final Mqtt3AsyncClient client;
    private final Mqtt3ClientConfigurationProperties configurationProperties;

    public Mqtt3ClientAdapter(@NotNull final Mqtt3AsyncClient client, @NotNull final Mqtt3ClientConfigurationProperties configurationProperties) {
        this.client = client;
        this.configurationProperties = configurationProperties;
    }

    @Override
    public void subscribe(final String[] topics, final int[] qos, final Consumer<MqttBindingContext<MqttMessage>> callback) {

        final Map<String, Integer> topicMap = getTopicMap(topics, qos);

        final Mqtt3Subscribe mqttSubscribe = Mqtt3Subscribe.builder()
            .addSubscriptions(
                topicMap.entrySet().stream().map(topicEntry -> Mqtt3Subscription.builder()
                    .topicFilter(topicEntry.getKey())
                    .qos(Objects.requireNonNull(MqttQos.fromCode(topicEntry.getValue())))
                    .build())
            ).build();

        client.subscribe(mqttSubscribe, mqtt3Publish -> {
                LOG.trace("Received message: {}", new String(mqtt3Publish.getPayloadAsBytes()));

                // TODO: populate MqttMessage properly
                final MqttMessage mqttMessage = new MqttMessage(mqtt3Publish.getPayloadAsBytes());
                mqttMessage.setQos(mqtt3Publish.getQos().getCode());

                final MqttV3BindingContext context = new MqttV3BindingContext(client, mqttMessage);
                context.setTopic(mqtt3Publish.getTopic().toString());
                context.setMqtt3Publish(mqtt3Publish);

                context.setManualAcks(configurationProperties.getManualAcks());
                callback.accept(context);

            }, configurationProperties.getManualAcks())
            .whenComplete((mqtt3SubAck, throwable) -> {
                if (throwable != null) {
                    throw new MqttSubscriberException(String.format("Failed to subscribe to the topics: %s", throwable.getMessage()), throwable);
                }
            });
    }

    @Override
    public void unsubscribe(final Set<String> topics) {
        final Mqtt3Unsubscribe mqtt3Unsubscribe = Mqtt3Unsubscribe.builder()
            .addTopicFilters(
                topics.stream().map(MqttTopicFilter::of)
            ).build();

        client.unsubscribe(mqtt3Unsubscribe);
    }

    @Override
    public boolean isConnected() {
        return client.getState().isConnected();
    }

    @Override
    public String getClientIdentifier() {
        return client.getConfig().getClientIdentifier().map(Object::toString).orElse(null);
    }
}
