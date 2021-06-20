/*
 * Copyright 2017-2021 original authors
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
package io.micronaut.mqtt.v5.client.health;

import java.util.Collections;

import javax.inject.Singleton;

import io.micronaut.core.util.StringUtils;
import org.eclipse.paho.mqttv5.client.MqttAsyncClient;
import org.reactivestreams.Publisher;

import io.micronaut.context.annotation.Requires;
import io.micronaut.health.HealthStatus;
import io.micronaut.management.endpoint.health.HealthEndpoint;
import io.micronaut.management.health.indicator.HealthIndicator;
import io.micronaut.management.health.indicator.HealthResult;
import io.reactivex.Flowable;

/**
 * A {@link HealthIndicator} for Mqtt Client.
 * @author Charlie Kapopoulos
 * @since 1.0.0
 */
@Requires(property = HealthEndpoint.PREFIX + ".mqtt.client.enabled", value = StringUtils.TRUE)
@Requires(beans = HealthEndpoint.class)
@Singleton
public class MqttHealthIndicator implements HealthIndicator {
    public static final String NAME = "mqtt-client";
    private MqttAsyncClient client;

    /**
     * Constructor.
     *
     * @param client MqttAsyncClient.
     */
    public MqttHealthIndicator(MqttAsyncClient client) {
        this.client = client;
    }

    @Override
    public Publisher<HealthResult> getResult() {
        HealthStatus status = client.isConnected() ? HealthStatus.UP : HealthStatus.DOWN;
        HealthResult.Builder builder = HealthResult.builder(NAME, status).details(Collections.singletonMap("clientId", client.getClientId()));
        return Flowable.just(builder.build());
    }
}
