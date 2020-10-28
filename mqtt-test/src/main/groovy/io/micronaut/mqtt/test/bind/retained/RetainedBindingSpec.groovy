package io.micronaut.mqtt.test.bind.retained

import io.micronaut.context.annotation.Requires
import io.micronaut.core.util.StringUtils
import io.micronaut.mqtt.annotation.MqttSubscriber
import io.micronaut.mqtt.annotation.Topic
import io.micronaut.mqtt.test.AbstractMQTTTest
import spock.lang.Ignore
import spock.util.concurrent.PollingConditions

abstract class RetainedBindingSpec extends AbstractMQTTTest {

    @Ignore
    void "test retained binding method overrides class"() {
        def ctx = startContext() //the subscriber is disabled
        def client = ctx.getBean(getClient())
        //publish a message before there is a subscriber
        client.override()
        ctx.close()

        when:
        ctx = startContext("retainedbindingspec": true)
        def sub = ctx.getBean(MySubscriber)
        def polling = new PollingConditions(timeout: 3)

        then: //the subscriber received the message
        polling.eventually {
            assert sub.received
        }

        cleanup:
        ctx.close()
    }

    void "test retained binding class value is used"() {
        def ctx = startContext() //the subscriber is disabled
        def client = ctx.getBean(getClient())
        //publish a message before there is a subscriber
        client.classLevel()
        ctx.close()

        when:
        ctx = startContext("retainedbindingspec": true)
        def sub = ctx.getBean(MySubscriber)
        def polling = new PollingConditions(timeout: 3)

        then: //the subscriber didn't receive the message
        polling.eventually {
            assert !sub.received
        }

        cleanup:
        ctx.close()
    }


    void "test retained binding argument set to false"() {
        def ctx = startContext() //the subscriber is disabled
        def client = ctx.getBean(getClient())
        //publish a message before there is a subscriber
        client.argument(false)
        ctx.close()

        when:
        ctx = startContext("retainedbindingspec": true)
        def sub = ctx.getBean(MySubscriber)
        def polling = new PollingConditions(timeout: 3)

        then: //the subscriber didn't receive the message
        polling.eventually {
            assert !sub.received
        }

        cleanup:
        ctx.close()
    }

    @Ignore
    void "test retained binding argument set to true"() {
        def ctx = startContext() //the subscriber is disabled
        def client = ctx.getBean(getClient())
        //publish a message before there is a subscriber
        client.argument(true)
        ctx.close()

        when:
        ctx = startContext("retainedbindingspec": true)
        def sub = ctx.getBean(MySubscriber)
        def polling = new PollingConditions(timeout: 3)

        then: //the subscriber received the message
        polling.eventually {
            assert sub.received
        }

        cleanup:
        ctx.close()
    }

    abstract Class<? extends RetainedBindingClient> getClient()

    @Requires(property = "retainedbindingspec", value = StringUtils.TRUE)
    @MqttSubscriber
    static class MySubscriber {

        Boolean received = null

        @Topic("test/retained")
        void get() {
            this.received = true
        }
    }
}
