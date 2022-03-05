/*
 * Copyright 2017-2022 original authors
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
package io.micronaut.mqtt.bind;

import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.type.Argument;

import java.util.Optional;

/**
 * Argument binder to bind publisher and subscriber method arguments
 * to and from the message context.
 *
 * @param <M> The message context
 * @param <T> The type
 */
public interface MqttBinder<M, T> {

    /**
     * Binds the given value to the given context. Publisher
     * method arguments will be passed to this method.
     *
     * @param context The message context
     * @param value The argument value
     * @param argument The publisher argument
     */
    void bindTo(M context, T value, Argument<T> argument);

    /**
     * Binds the given context and returns an optional value. Subscriber
     * method arguments will be passed to this method.
     *
     * @param context The method context
     * @param conversionContext The argument conversion context
     * @return An optional value
     */
    Optional<T> bindFrom(M context, ArgumentConversionContext<T> conversionContext);
}
