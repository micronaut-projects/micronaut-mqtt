package io.micronaut.mqtt.docs.custom.annotation

// tag::imports[]
import io.micronaut.core.type.Argument
import io.micronaut.mqtt.bind.AnnotatedMqttBinder
import io.micronaut.mqtt.v5.bind.MqttV5BindingContext
import io.micronaut.core.convert.ArgumentConversionContext
import io.micronaut.core.convert.ConversionService

import javax.inject.Singleton
// end::imports[]

import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "CorrelationSpec")
// tag::clazz[]
@Singleton // <1>
class CorrelationAnnotationBinder implements AnnotatedMqttBinder<MqttV5BindingContext, Correlation> { // <2>

    private final ConversionService conversionService

    CorrelationAnnotationBinder(ConversionService conversionService) { // <3>
        this.conversionService = conversionService
    }

    @Override
    Class<Correlation> getAnnotationType() {
        Correlation
    }

    @Override
    void bindTo(MqttV5BindingContext context, Object value, Argument<Object> argument) {
        context.properties.correlationData = (byte[]) value // <4>
    }

    @Override
    Optional<Object> bindFrom(MqttV5BindingContext context, ArgumentConversionContext<Object> conversionContext) {
        conversionService.convert(context.properties.correlationData, conversionContext) // <5>
    }
}
// end::clazz[]
