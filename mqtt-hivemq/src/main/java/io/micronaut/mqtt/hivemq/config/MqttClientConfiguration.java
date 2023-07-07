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
package io.micronaut.mqtt.hivemq.config;

import java.net.URI;
import java.time.Duration;

/**
 * @author Sergio del Amo
 * @since 3.0.0
 */
public interface MqttClientConfiguration {

    /**
     * @return the uri of server to connect to as [schema]://[serverHost]:[serverPort].
     */
    URI getServerUri();


    /**
     * @return the client identifier.
     */
    String getClientId();


    /**
     * @return an optional boolean to set the client in manual acknowledge mode.
     */
    boolean getManualAcks();


    /**
     * @return the connection timeout.
     */
    public Duration getConnectionTimeout();
}
