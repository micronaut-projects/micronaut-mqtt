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
package io.micronaut.mqtt.annotation;

import io.micronaut.core.bind.annotation.Bindable;

import java.lang.annotation.*;

/**
 * Used for binding the message topic and other parameters. Use multiple annotations to
 * subscribe to multiple topics.
 *
 * When used on a subscriber, any topic annotations from the class level will be used
 * in <b>addition</b> to any annotations found on the method level.
 *
 * Only one instance of the annotation can be applied to publishers because publishers
 * cannot publish to multiple topics.
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER})
@Repeatable(value = Topics.class)
@Bindable
public @interface Topic {

    String value();

    int qos() default 1;
}
