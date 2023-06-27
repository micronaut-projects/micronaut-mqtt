package io.micronaut.mqtt.docs.custom.type


import io.micronaut.context.ApplicationContext
import io.micronaut.mqtt.test.AbstractMQTTTest
import io.micronaut.mqtt.test.MQTT5Test
import spock.util.concurrent.PollingConditions

class ProductInfoSpec extends AbstractMQTTTest implements MQTT5Test {

    void "test using a custom type binder"() {
        ApplicationContext applicationContext = startContext()
        PollingConditions conditions = new PollingConditions(timeout: 5)

        when:
// tag::producer[]
        ProductClient productClient = applicationContext.getBean(ProductClient)
        productClient.send("body".bytes, new ProductInfo("small", 10L, true))
        productClient.send("body2".bytes, new ProductInfo("medium", 20L, true))
        productClient.send("body3".bytes, new ProductInfo(null, 30L, true))
// end::producer[]

        ProductListener productListener = applicationContext.getBean(ProductListener)

        then:
        conditions.eventually {
            productListener.messages.size() == 3
            productListener.messages.find({ pi ->
                pi.size == "small" && pi.count == 10 && pi.sealed
            }) != null
            productListener.messages.find({ pi ->
                pi.size == "medium" && pi.count == 20 && pi.sealed
            }) != null
            productListener.messages.find({ pi ->
                pi.size == null && pi.count == 30 && pi.sealed
            }) != null
        }

        cleanup:
        applicationContext.close()
    }
}
