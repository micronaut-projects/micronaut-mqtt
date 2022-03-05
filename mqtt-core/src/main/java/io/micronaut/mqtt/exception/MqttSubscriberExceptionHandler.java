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
package io.micronaut.mqtt.exception;

import io.micronaut.context.annotation.DefaultImplementation;
import io.micronaut.core.exceptions.ExceptionHandler;

/**
 * Responsible for handling exceptions thrown during the subscription process. Can be
 * implemented in {@link io.micronaut.mqtt.annotation.MqttSubscriber} beans to handle
 * exceptions thrown specific to that class.
 *
 * Replace this with your own {@link io.micronaut.context.annotation.Primary} bean to handle
 * exceptions not handled by any other listeners.
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@DefaultImplementation(DefaultMqttSubscriberExceptionHandler.class)
public interface MqttSubscriberExceptionHandler extends ExceptionHandler<MqttSubscriberException> {
}
