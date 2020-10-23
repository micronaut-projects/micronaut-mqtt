package io.micronaut.mqtt.annotation;

import io.micronaut.context.annotation.AliasFor;
import io.micronaut.core.bind.annotation.Bindable;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Bindable
public @interface Topic {

    String value() default "";

    @AliasFor(annotation = Qos.class, member = "value")
    int qos() default 1;

    @AliasFor(annotation = Retained.class, member = "value")
    boolean retained() default false;
}
