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
package io.micronaut.mqtt.hivemq.client;

import io.micronaut.mqtt.bind.MqttBindingContext;
import io.micronaut.mqtt.hivemq.bind.MqttMessage;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Common interface for HiveMQ MQTT clients.
 *
 * @author Sven Kobow
 * @since 3.0.0
 */
public interface MqttClientAdapter {

    void subscribe(String[] topics, int[] qos, Consumer<MqttBindingContext<MqttMessage>> callback);

    default Map<String, Integer> getTopicMap(String[] topics, int[] qos) {
        return IntStream.range(0, topics.length).boxed()
            .collect(Collectors.toMap(i -> topics[i], i -> qos[i]));
    }

    void unsubscribe(Set<String> topics);

    boolean isConnected();

    Object getClientIdentifier();
}
