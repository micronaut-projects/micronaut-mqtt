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
package io.micronaut.mqtt.hivemq.client.health;

import io.micronaut.context.annotation.Requires;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.util.StringUtils;
import io.micronaut.health.HealthStatus;
import io.micronaut.management.endpoint.health.HealthEndpoint;
import io.micronaut.management.health.indicator.HealthIndicator;
import io.micronaut.management.health.indicator.HealthResult;
import io.micronaut.mqtt.hivemq.client.MqttClientAdapter;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;

import java.util.Collections;

@Requires(property = HealthEndpoint.PREFIX + ".mqtt.client.enabled", value = StringUtils.TRUE)
@Requires(beans = HealthEndpoint.class)
@Singleton
public class MqttHealthIndicator implements HealthIndicator {
    public static final String NAME = "mqtt-client";
    private final MqttClientAdapter client;

    public MqttHealthIndicator(final MqttClientAdapter client) {
        this.client = client;
    }

    @Override
    public Publisher<HealthResult> getResult() {
        HealthStatus status = client.isConnected() ? HealthStatus.UP : HealthStatus.DOWN;
        HealthResult.Builder builder = HealthResult.builder(NAME, status).details(Collections.singletonMap("clientId", client.getClientIdentifier()));
        return Publishers.just(builder.build());
    }
}
