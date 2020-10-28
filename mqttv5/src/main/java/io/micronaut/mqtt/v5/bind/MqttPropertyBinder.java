package io.micronaut.mqtt.v5.bind;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.mqtt.bind.AnnotatedMqttBinder;
import io.micronaut.mqtt.v5.annotation.MqttProperty;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;

import java.util.Optional;

@Introspected(classes = MqttProperties.class)
public class MqttPropertyBinder implements AnnotatedMqttBinder<MqttV5BindingContext, MqttProperty> {

    private final ConversionService<?> conversionService;
    private final BeanIntrospection<MqttProperties> introspection;

    public MqttPropertyBinder(ConversionService<?> conversionService) {
        this.conversionService = conversionService;
        this.introspection = BeanIntrospection.getIntrospection(MqttProperties.class);
    }

    @Override
    public Class<MqttProperty> getAnnotationType() {
        return MqttProperty.class;
    }

    @Override
    public void bindTo(MqttV5BindingContext context, Object value, Argument<?> argument) {
        String name = getParameterName(argument);
        introspection.getProperty(name).ifPresent(bp -> bp.convertAndSet(context.getProperties(), value));
    }

    @Override
    public Optional<?> bindFrom(MqttV5BindingContext context, ArgumentConversionContext<?> conversionContext) {
        String name = getParameterName(conversionContext.getArgument());
        return introspection.getProperty(name)
                .map(bp -> bp.get(context.getProperties()))
                .flatMap(val -> conversionService.convert(val, conversionContext));

    }

    private String getParameterName(Argument<?> argument) {
        return argument.getAnnotationMetadata().getValue(MqttProperty.class, String.class).orElse(argument.getName());
    }

}
