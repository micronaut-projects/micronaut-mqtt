package io.micronaut.mqtt.docs.properties

import io.micronaut.mqtt.test.AbstractMQTTTest
import io.micronaut.context.ApplicationContext
import io.micronaut.mqtt.test.MQTT5Test
import spock.util.concurrent.PollingConditions

class PropertiesSpec extends AbstractMQTTTest implements MQTT5Test {

    void "test sending and receiving properties"() {
        ApplicationContext applicationContext = startContext()
        PollingConditions conditions = new PollingConditions(timeout: 5)

        when:
// tag::producer[]
        ProductClient productClient = applicationContext.getBean(ProductClient)
        productClient.send("body".bytes)
        productClient.send("guest", "text/html", "body2".bytes)
        productClient.send("guest", null, "body3".bytes)
// end::producer[]

        ProductListener productListener = applicationContext.getBean(ProductListener)

        then:
        conditions.eventually {
            productListener.messageProperties.size() == 3
            productListener.messageProperties.contains("guest|application/json|myApp")
            productListener.messageProperties.contains("guest|text/html|myApp")
            productListener.messageProperties.contains("guest|null|myApp")
        }

        cleanup:
        applicationContext.close()
    }
}
