package io.micronaut.mqtt.docs.serdes;

// tag::imports[]
import io.micronaut.mqtt.annotation.Topic;
import io.micronaut.mqtt.annotation.MqttSubscriber;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
// end::imports[]

import io.micronaut.context.annotation.Requires;

@Requires(property = "spec.name", value = "ProductInfoSerDesSpec")
// tag::clazz[]
@MqttSubscriber
public class ProductListener {

    List<ProductInfo> messages = Collections.synchronizedList(new ArrayList<>());

    @Topic("product")
    public void receive(ProductInfo productInfo) { // <1>
        messages.add(productInfo);
    }
}
// end::clazz[]
