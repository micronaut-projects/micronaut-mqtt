package io.micronaut.mqtt.docs.quickstart

import io.micronaut.mqtt.test.AbstractMQTTTest
import io.micronaut.context.ApplicationContext
import io.micronaut.mqtt.test.MQTT5Test
import spock.util.concurrent.PollingConditions

class QuickstartSpec extends AbstractMQTTTest implements MQTT5Test {

    void "test product client and listener"() {
        ApplicationContext applicationContext = startContext()
        PollingConditions conditions = new PollingConditions(timeout: 5)

        when:
// tag::producer[]
def productClient = applicationContext.getBean(ProductClient)
productClient.send("quickstart".getBytes())
// end::producer[]

        ProductListener productListener = applicationContext.getBean(ProductListener)

        then:
        conditions.eventually {
            productListener.messageLengths.size() == 1
            productListener.messageLengths[0] == "quickstart"
        }

        cleanup:
        // Finding that the context is closing the channel before ack is sent
        Thread.sleep(200)
        applicationContext.close()
    }
}
