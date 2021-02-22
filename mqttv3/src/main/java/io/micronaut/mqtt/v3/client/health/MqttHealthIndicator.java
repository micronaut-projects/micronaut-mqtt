package io.micronaut.mqtt.v3.client.health;

import java.util.Collections;

import javax.inject.Singleton;

import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.reactivestreams.Publisher;

import io.micronaut.context.annotation.Requires;
import io.micronaut.health.HealthStatus;
import io.micronaut.management.endpoint.health.HealthEndpoint;
import io.micronaut.management.health.indicator.HealthIndicator;
import io.micronaut.management.health.indicator.HealthResult;
import io.reactivex.Flowable;

/**
 * A {@link HealthIndicator} for Mqtt Client.
 *
 */
@Requires(property = HealthEndpoint.PREFIX + ".mqtt.client.enabled", value = "true")
@Requires(beans = HealthEndpoint.class)
@Singleton
public class MqttHealthIndicator implements HealthIndicator {
	public static final String NAME = "mqtt-client-health";
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
		HealthResult.Builder builder = HealthResult.builder(NAME, status).details(Collections.singletonMap("class", client.getClass().getName()));
		return Flowable.just(builder.build());
	}
}
