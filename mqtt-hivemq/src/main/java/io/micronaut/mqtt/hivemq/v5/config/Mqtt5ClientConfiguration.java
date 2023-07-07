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
package io.micronaut.mqtt.hivemq.v5.config;

import io.micronaut.mqtt.hivemq.config.MqttClientConfiguration;

import java.util.Map;

/**
 * MQTT v5 Client Configuration.
 * @author Sergio del Amo
 * @since 3.0.0
 */
public interface Mqtt5ClientConfiguration extends MqttClientConfiguration {
    /**
     * @return true if a new sessions should be started for connection.
     */
    boolean isCleanStart();

    /**
     * @return the session expiry interval in seconds.
     */
    Long getSessionExpiryInterval();

    /**
     * @return the maximum amount of not acknowledged publishes with QoS 1 or 2 the client accepts from the server concurrently.
     */
    Integer getReceiveMaximum();

    /**
     * @return the maximum packet size the client sends to the server.
     */
    Integer getMaximumPacketSize();

    /**
     * @return the maximum amount of topic aliases the client accepts from the server.
     */
    Integer getTopicAliasMaximum();

    /**
     * @return whether the client requests response information from the server.
     */
    boolean isRequestResponseInfo();

    /**
     * @return whether the client requests problem information from the server.
     */
    boolean isRequestProblemInfo();

    /**
     * @return the user defined properties tha should be sent for every message.
     */
    Map<String, String> getUserProperties();

}
