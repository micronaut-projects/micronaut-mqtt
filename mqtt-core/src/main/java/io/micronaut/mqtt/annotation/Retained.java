package io.micronaut.mqtt.annotation;

import io.micronaut.core.bind.annotation.Bindable;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER})
@Bindable
public @interface Retained {

    boolean value() default false;
}
