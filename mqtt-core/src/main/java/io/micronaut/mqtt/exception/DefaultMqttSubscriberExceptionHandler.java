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

import io.micronaut.context.annotation.Primary;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The exception handler for subscriber exceptions that is used if the subscriber
 * does not implement {@link MqttSubscriberExceptionHandler}.
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@Singleton
@Primary
public class DefaultMqttSubscriberExceptionHandler implements MqttSubscriberExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultMqttSubscriberExceptionHandler.class);

    @Override
    public void handle(MqttSubscriberException exception) {
        if (LOG.isErrorEnabled()) {
            LOG.error(exception.getMessage(), exception);
        }
    }
}
