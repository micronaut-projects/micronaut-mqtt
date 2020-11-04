/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.mqtt.v5.annotation;

import io.micronaut.core.bind.annotation.Bindable;

import java.lang.annotation.*;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Used for binding arguments to {@link org.eclipse.paho.mqttv5.common.packet.MqttProperties}.
 *
 * @author James Kleeh
 * @since 1.0.0
 */
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
     * a class or method. If the name is not one of the supported property names, the
     * name and value will be populated via a {@link org.eclipse.paho.mqttv5.common.packet.UserProperty}.
     *
     * @see org.eclipse.paho.mqttv5.common.packet.MqttProperties for a list of names
     * @return The name of property
     */
    String name() default "";
}
