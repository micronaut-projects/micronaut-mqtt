package io.micronaut.mqtt.hivemq.v5.client

import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectReasonCode
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.core.util.StringUtils
import io.micronaut.mqtt.annotation.MqttSubscriber
import io.micronaut.mqtt.annotation.Topic
import io.micronaut.mqtt.test.AbstractMQTTTest
import io.micronaut.mqtt.test.MQTT5Test
import spock.util.concurrent.PollingConditions

class V5WillMessageSpec extends AbstractMQTTTest implements MQTT5Test {

    void "test will message"() {
        // we create first application context that is going to disconnect.
        ApplicationContext ctx = startContext([
                "mqtt.client.will-message.payload":"last will",
                "mqtt.client.will-message.retained":true,
                "mqtt.client.will-message.topic":"will/topic",
                ])
        def client = ctx.getBean(Mqtt5AsyncClient)
        def polling = new PollingConditions(timeout: 3)

        // second application is going to subscribe to the will message topic
        // and track it to see if it gets published when first application disconnects
        ApplicationContext ctx2 = startContext([
                "willmessagetest": true])
        def subscriber = ctx2.getBean(LastWillSubscriber)


        when:
        client.disconnectWith()
                .reasonCode(Mqtt5DisconnectReasonCode.DISCONNECT_WITH_WILL_MESSAGE) // send the will message
                .sessionExpiryInterval(0)                                           // we want to clear the session
                .send()


        then:
        polling.eventually {
            assert subscriber.payload == "last will"
        }

        cleanup:
        ctx.close()
        ctx2.close()
    }

    @Requires(property = "willmessagetest", value = StringUtils.TRUE)
    @MqttSubscriber
    static class LastWillSubscriber {

        String payload

        @Topic("will/topic")
        void get(String payload) {
            this.payload = payload
        }
    }
}
