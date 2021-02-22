package io.micronaut.mqtt.health

import io.micronaut.context.ApplicationContext
import io.micronaut.health.HealthStatus
import io.micronaut.management.health.indicator.HealthResult
import io.micronaut.mqtt.test.AbstractMQTTTest
import io.micronaut.mqtt.v3.client.health.MqttHealthIndicator
import io.reactivex.Flowable
import org.eclipse.paho.client.mqttv3.MqttAsyncClient

class V3HealthIndicatorSpec extends AbstractMQTTTest {

    void "mqtt v3 client health indicator"() {
        ApplicationContext ctx = startContext()
        def mqttClient = ctx.getBean(MqttAsyncClient.class)

        when:
        def indicator = ctx.getBean(MqttHealthIndicator.class)
        HealthResult result = Flowable.fromPublisher(indicator.getResult()).blockingFirst()

        then:
        result.status == HealthStatus.UP
        result.details['class'] == mqttClient.class.name

        when:
        mqttClient.disconnect()
        result = Flowable.fromPublisher(indicator.getResult()).blockingFirst()

        then:
        result.status == HealthStatus.DOWN

        cleanup:
        ctx.close()
    }
}
