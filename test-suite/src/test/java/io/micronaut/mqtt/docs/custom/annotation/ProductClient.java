package io.micronaut.mqtt.docs.custom.annotation;

// tag::imports[]
import io.micronaut.mqtt.annotation.Topic;
import io.micronaut.mqtt.annotation.v5.MqttPublisher;
// end::imports[]

import io.micronaut.context.annotation.Requires;

@Requires(property = "spec.name", value = "CorrelationSpec")
// tag::clazz[]
@MqttPublisher
public interface ProductClient {

    @Topic("product")
    void send(@Correlation byte[] correlation);
}
// end::clazz[]
