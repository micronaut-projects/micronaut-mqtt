package io.micronaut.mqtt.health

import io.micronaut.context.ApplicationContext
import io.micronaut.health.HealthStatus
import io.micronaut.management.health.indicator.HealthResult
import io.micronaut.mqtt.test.AbstractMQTTTest
import io.micronaut.mqtt.v5.client.health.MqttHealthIndicator
import io.reactivex.Flowable
import org.eclipse.paho.mqttv5.client.MqttAsyncClient

class V5HealthIndicatorSpec extends AbstractMQTTTest {

    void "mqtt v5 client health indicator"() {
        ApplicationContext ctx = startContext()
        def mqttClient = ctx.getBean(MqttAsyncClient.class)

        when:
        def indicator = ctx.getBean(MqttHealthIndicator.class)
        HealthResult result = Flowable.fromPublisher(indicator.getResult()).blockingFirst()

        then:
        result.status == HealthStatus.UP
        result.details['clientId'] != null

        when:
        mqttClient.disconnect()
        result = Flowable.fromPublisher(indicator.getResult()).blockingFirst()

        then:
        result.status == HealthStatus.DOWN

        cleanup:
        ctx.close()
    }
}
