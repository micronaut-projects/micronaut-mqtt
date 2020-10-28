package io.micronaut.mqtt.v5.annotation;

import io.micronaut.core.bind.annotation.Bindable;

import java.lang.annotation.*;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Retention(RUNTIME)
@Target({ElementType.PARAMETER, ElementType.TYPE, ElementType.METHOD})
@Repeatable(value = MqttProperties.class)
@Bindable
public @interface MqttProperty {

    /**
     * If used as a bound parameter, this is the property name. If used on a class
     * level this is value and not the property name.
     *
     * @return The name of the property, otherwise it is inferred from the {@link #name()}
     */
    String value() default "";

    /**
     * Never used if applied to a parameter. Supplies the property name if used on
     * a class or method.
     *
     * @return The name of property
     */
    String name() default "";
}
