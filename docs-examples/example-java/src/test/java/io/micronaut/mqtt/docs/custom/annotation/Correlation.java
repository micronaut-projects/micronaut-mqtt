package io.micronaut.mqtt.docs.custom.annotation;

// tag::imports[]
import io.micronaut.core.bind.annotation.Bindable;

import java.lang.annotation.*;
// end::imports[]

// tag::clazz[]
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
@Bindable // <1>
public @interface Correlation {
}
// end::clazz[]
