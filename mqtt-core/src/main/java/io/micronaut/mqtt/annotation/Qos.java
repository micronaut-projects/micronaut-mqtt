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
 * Used for binding the message Qos. This annotation should not be used on subscriber
 * classes or methods as the Qos must be set per topic. Use {@link Topic#qos()} instead.
 * The annotation may be applied to publisher classes and methods because a publisher may only
 * publish to a single topic.
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER})
@Bindable
@Inherited
public @interface Qos {

    int DEFAULT_VALUE = 1;

    int value() default DEFAULT_VALUE;
}
