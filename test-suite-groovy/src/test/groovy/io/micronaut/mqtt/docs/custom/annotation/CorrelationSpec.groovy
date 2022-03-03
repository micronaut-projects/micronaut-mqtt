package io.micronaut.mqtt.docs.custom.annotation

import io.micronaut.context.ApplicationContext
import io.micronaut.mqtt.test.AbstractMQTTTest
import spock.util.concurrent.PollingConditions

class CorrelationSpec extends AbstractMQTTTest {

    void "test using a custom annotation binder"() {
        ApplicationContext applicationContext = startContext()
        PollingConditions conditions = new PollingConditions(timeout: 10)

        when:
// tag::producer[]
        ProductClient productClient = applicationContext.getBean(ProductClient)
        productClient.send("a".bytes)
        productClient.send("b".bytes)
        productClient.send("c".bytes)
// end::producer[]

        ProductListener productListener = applicationContext.getBean(ProductListener)

        then:
        conditions.eventually {
            productListener.messages.size() == 3
            productListener.messages == ["a", "b", "c"] as Set
        }

        cleanup:
        applicationContext.close()
    }
}
