package io.micronaut.mqtt.docs.parameters

import io.micronaut.mqtt.test.AbstractMQTTTest
import io.micronaut.context.ApplicationContext
import io.micronaut.mqtt.test.MQTT5Test
import spock.util.concurrent.PollingConditions

class BindingSpec extends AbstractMQTTTest implements MQTT5Test {

    void "test dynamic binding"() {
        ApplicationContext applicationContext = startContext()
        PollingConditions conditions = new PollingConditions(timeout: 5)

        when:
// tag::producer[]
        def productClient = applicationContext.getBean(ProductClient)
        productClient.send("message body".bytes)
        productClient.send("product", "message body2".bytes)
// end::producer[]

        ProductListener productListener = applicationContext.getBean(ProductListener)

        then:
        conditions.eventually {
            productListener.messageLengths.size() == 2
            productListener.messageLengths.contains(12)
            productListener.messageLengths.contains(13)
        }

        cleanup:
        applicationContext.close()
    }
}
