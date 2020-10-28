package io.micronaut.mqtt.v5.annotation;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface MqttProperties {

    MqttProperty[] value() default {};
}
