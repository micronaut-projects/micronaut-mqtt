package io.micronaut.mqtt.bind.property

import edu.umd.cs.findbugs.annotations.Nullable
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.mqtt.annotation.MqttSubscriber
import io.micronaut.mqtt.annotation.Topic
import io.micronaut.mqtt.test.AbstractMQTTTest
import io.micronaut.mqtt.v5.annotation.MqttPublisher
import io.micronaut.mqtt.v5.annotation.MqttProperty
import spock.util.concurrent.PollingConditions

class V5PropertyBindingSpec extends AbstractMQTTTest {

    void "test property binding"() {
        ApplicationContext ctx = startContext()
        def client = ctx.getBean(MyClient)
        def sub = ctx.getBean(MySubscriber)
        def polling = new PollingConditions(timeout: 3)

        when:
        client.send()

        then:
        polling.eventually {
            assert sub.contentType == "application/json"
            assert sub.nullable == null
        }

        when:
        client.sendArg("application/xml")

        then:
        polling.eventually {
            assert sub.contentType == "application/xml"
            assert sub.nullable == null
        }

        when:
        client.sendCustom("this is a user property")

        then:
        polling.eventually {
            assert sub.customProp == "this is a user property"
        }

        cleanup:
        ctx.close()
    }

    @Requires(property = "spec.name", value = "V5PropertyBindingSpec")
    @Topic("test/property")
    @MqttPublisher
    static interface MyClient {

        @MqttProperty(name = "contentType", value = "application/json")
        void send()

        void sendArg(@MqttProperty(name = "contentType") String contentType)

        @Topic("test/property/custom")
        void sendCustom(@MqttProperty String customUserProperty)
    }

    @Requires(property = "spec.name", value = "V5PropertyBindingSpec")
    @MqttSubscriber
    static class MySubscriber {

        String contentType
        String nullable
        String customProp

        @Topic("test/property")
        void get(@MqttProperty String contentType, @Nullable @MqttProperty String nullable) {
            this.contentType = contentType
            this.nullable = nullable
        }

        @Topic("test/property/custom")
        void getCustom(@MqttProperty("customUserProperty") String cup) {
            this.customProp = cup
        }
    }
}
