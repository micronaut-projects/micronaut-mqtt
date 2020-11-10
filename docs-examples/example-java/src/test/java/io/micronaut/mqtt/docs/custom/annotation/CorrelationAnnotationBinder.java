package io.micronaut.mqtt.docs.custom.annotation;

// tag::imports[]
import io.micronaut.core.type.Argument;
import io.micronaut.mqtt.bind.AnnotatedMqttBinder;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.mqtt.v5.bind.MqttV5BindingContext;

import javax.inject.Singleton;
import java.util.Optional;
// end::imports[]

import io.micronaut.context.annotation.Requires;

@Requires(property = "spec.name", value = "CorrelationSpec")
// tag::clazz[]
@Singleton // <1>
public class CorrelationAnnotationBinder implements AnnotatedMqttBinder<MqttV5BindingContext, Correlation> { // <2>

    private final ConversionService<?> conversionService;

    public CorrelationAnnotationBinder(ConversionService<?> conversionService) { // <3>
        this.conversionService = conversionService;
    }

    @Override
    public Class<Correlation> getAnnotationType() {
        return Correlation.class;
    }

    @Override
    public void bindTo(MqttV5BindingContext context, Object value, Argument<Object> argument) {
        context.getProperties().setCorrelationData((byte[]) value); // <4>
    }

    @Override
    public Optional<Object> bindFrom(MqttV5BindingContext context, ArgumentConversionContext<Object> conversionContext) {
        return conversionService.convert(context.getProperties().getCorrelationData(), conversionContext); // <5>
    }
}
// end::clazz[]
