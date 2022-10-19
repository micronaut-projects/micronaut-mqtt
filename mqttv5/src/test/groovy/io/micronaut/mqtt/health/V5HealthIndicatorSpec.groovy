package io.micronaut.mqtt.health

import io.micronaut.context.ApplicationContext
import io.micronaut.health.HealthStatus
import io.micronaut.management.health.indicator.HealthResult
import io.micronaut.mqtt.test.AbstractMQTTTest
import io.micronaut.mqtt.v5.client.health.MqttHealthIndicator
import org.eclipse.paho.mqttv5.client.MqttAsyncClient
import reactor.core.publisher.Flux

class V5HealthIndicatorSpec extends AbstractMQTTTest {

    void "mqtt v5 client health indicator"() {
        ApplicationContext ctx = startContext()
        def mqttClient = ctx.getBean(MqttAsyncClient.class)

        when:
        def indicator = ctx.getBean(MqttHealthIndicator.class)
        HealthResult result = Flux.from(indicator.getResult()).blockFirst()

        then:
        result.status == HealthStatus.UP
        result.details['clientId'] != null

        when:
        mqttClient.disconnect()
        result = Flux.from(indicator.getResult()).blockFirst()

        then:
        result.status == HealthStatus.DOWN
    }
}
