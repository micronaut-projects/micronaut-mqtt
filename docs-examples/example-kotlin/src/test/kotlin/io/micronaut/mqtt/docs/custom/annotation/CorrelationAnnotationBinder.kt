package io.micronaut.mqtt.docs.custom.annotation

// tag::imports[]
import io.micronaut.core.convert.ArgumentConversionContext
import io.micronaut.core.convert.ConversionService
import io.micronaut.core.type.Argument
import io.micronaut.mqtt.bind.AnnotatedMqttBinder
import io.micronaut.mqtt.v5.bind.MqttV5BindingContext
import java.util.*

import jakarta.inject.Singleton
// end::imports[]

import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "CorrelationSpec")
// tag::clazz[]
@Singleton // <1>
class CorrelationAnnotationBinder(private val conversionService: ConversionService<*>)// <3>
    : AnnotatedMqttBinder<MqttV5BindingContext, Correlation> { // <2>

    override fun getAnnotationType(): Class<Correlation> {
        return Correlation::class.java
    }

    override fun bindTo(context: MqttV5BindingContext, value: Any, argument: Argument<Any>) {
       context.properties.correlationData = value as? ByteArray // <4>
    }

    override fun bindFrom(context: MqttV5BindingContext, conversionContext: ArgumentConversionContext<Any>): Optional<Any> {
        return conversionService.convert(context.properties.correlationData, conversionContext) // <5>
    }
}
// end::clazz[]
