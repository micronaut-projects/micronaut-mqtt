/*
 * Copyright 2017-2022 original authors
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
package io.micronaut.mqtt.bind;

import io.micronaut.core.annotation.Introspected;

/**
 * A generic context for binding to MQTT messages.
 *
 * @param <T> The message type
 * @author James Kleeh
 * @since 1.0.0
 */
@Introspected
public interface MqttBindingContext<T> {

    /**
     * @return The message payload
     */
    byte[] getPayload();

    /**
     * Sets the message payload.
     *
     * @param payload The payload
     */
    void setPayload(byte[] payload);

    /**
     * @return True if the message is retained
     */
    boolean isRetained();

    /**
     * Sets the message to be retained.
     *
     * @param retained The retained flag
     */
    void setRetained(boolean retained);

    /**
     * @return The message QOS
     */
    int getQos();

    /**
     * Sets the message qos. Must be 0, 1, or 2.
     * @param qos The qos
     */
    void setQos(int qos);

    /**
     * @return The message topic
     */
    String getTopic();

    /**
     * Sets the message topic.
     *
     * @param topic The topic
     */
    void setTopic(String topic);

    /**
     * @return The message ID
     */
    int getId();

    /**
     * Acknowledges a message.
     */
    void acknowlege();

    /**
     * @return The underlying message instance
     */
    T getNativeMessage();
}
