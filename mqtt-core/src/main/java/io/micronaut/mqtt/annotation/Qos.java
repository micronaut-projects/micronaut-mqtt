package io.micronaut.mqtt.annotation;

import io.micronaut.core.bind.annotation.Bindable;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER})
@Bindable
public @interface Qos {

    int DEFAULT_VALUE = 1;

    int value() default DEFAULT_VALUE;
}
