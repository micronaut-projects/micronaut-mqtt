package io.micronaut.mqtt.ssl

import io.micronaut.context.ApplicationContext
import spock.util.concurrent.PollingConditions

class SslAuthenticationSpec extends AbstractMQTTTest {

    void "test product client and listener"() {
        ApplicationContext applicationContext = startContext()
        PollingConditions conditions = new PollingConditions(timeout: 5)

        when:
        def productClient = applicationContext.getBean(ProductClient)
        productClient.send("quickstart".getBytes())

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
