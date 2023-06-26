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

import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.mqtt.hivemq.v3.config.Mqtt3ClientConfigurationProperties;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A factory to create an MQTT v3 client.
 *
 * @author Sven Kobow
 * @since 3.0.0
 */
@Factory
@Requires(property = "mqtt.client.mqtt-version", value = "5")
public final class Mqtt5ClientFactory {

    private static final Logger LOG = LoggerFactory.getLogger(Mqtt5ClientFactory.class);

    @Singleton
    @Bean(preDestroy = "disconnect")
    Mqtt5AsyncClient mqttClient(final Mqtt3ClientConfigurationProperties configurationProperties) {
        return null;
    }
}
