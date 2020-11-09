package io.micronaut.mqtt.docs.consumer.acknowledge.type


import io.micronaut.context.ApplicationContext
import io.micronaut.mqtt.test.AbstractMQTTTest
import spock.util.concurrent.PollingConditions

class AcknowledgeSpec extends AbstractMQTTTest {

    void "test acking with an acknowledgement argument"() {
        ApplicationContext applicationContext = startContext()
        PollingConditions conditions = new PollingConditions(timeout: 10)

        when:
// tag::producer[]
ProductClient productClient = applicationContext.getBean(ProductClient)
productClient.send("message body".bytes)
productClient.send("message body".bytes)
productClient.send("message body".bytes)
productClient.send("message body".bytes)
// end::producer[]

        ProductListener productListener = applicationContext.getBean(ProductListener)

        then:
        conditions.eventually {
            productListener.messageCount.get() == 4
        }

        cleanup:
        applicationContext.close()
    }
}
