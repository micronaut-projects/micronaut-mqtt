package io.micronaut.mqtt.test.bind.topic

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.core.util.StringUtils
import io.micronaut.mqtt.annotation.MqttSubscriber
import io.micronaut.mqtt.annotation.Topic
import io.micronaut.mqtt.test.AbstractMQTTTest
import spock.util.concurrent.PollingConditions

abstract class TopicBindingSpec extends AbstractMQTTTest {

    void "test topic binding"() {
        ApplicationContext ctx = startContext("topicbindingspec": true)
        def client = ctx.getBean(getClient())
        def sub = ctx.getBean(MySubscriber)
        def polling = new PollingConditions(timeout: 3)

        when:
        client.override()

        then:
        polling.eventually {
            assert sub.topic == "test/topic/override"
        }

        when:
        client.argument("test/topic/arg")

        then:
        polling.eventually {
            assert sub.topic == "test/topic/arg"
        }

        when:
        client.classLevelTopic()

        then:
        polling.eventually {
            assert sub.topic == "test/topic/classlevel"
        }

        cleanup:
        ctx.close()
    }

    abstract Class<? extends TopicBindingClient> getClient()

    @Requires(property = "topicbindingspec", value = StringUtils.TRUE)
    @MqttSubscriber
    static class MySubscriber {

        String topic

        @Topic("test/topic/#")
        void get(@Topic String topic) {
            this.topic = topic
        }
    }
}
