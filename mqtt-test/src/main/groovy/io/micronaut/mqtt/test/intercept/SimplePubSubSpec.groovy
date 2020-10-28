package io.micronaut.mqtt.test.intercept

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.core.util.StringUtils
import io.micronaut.mqtt.annotation.MqttSubscriber
import io.micronaut.mqtt.annotation.Topic
import io.micronaut.mqtt.test.AbstractMQTTTest
import spock.util.concurrent.PollingConditions

abstract class SimplePubSubSpec extends AbstractMQTTTest {

    void "test simple publishing and subscribing"() {
        ApplicationContext ctx = startContext("simplepubsubtest": true)
        def client = ctx.getBean(getClient())
        def subscriber = ctx.getBean(MySubscriber)
        def polling = new PollingConditions(timeout: 3)

        when:
        client.publish("test body")

        then:
        polling.eventually {
            assert subscriber.payload == "test body"
        }

        cleanup:
        ctx.close()
    }

    abstract Class<? extends SimplePubSubClient> getClient()

    @Requires(property = "simplepubsubtest", value = StringUtils.TRUE)
    @MqttSubscriber
    static class MySubscriber {

        String payload

        @Topic("test/simple")
        void get(String payload) {
            this.payload = payload
        }
    }
}
