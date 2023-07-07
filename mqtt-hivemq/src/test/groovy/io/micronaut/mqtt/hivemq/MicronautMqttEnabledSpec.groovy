package io.micronaut.mqtt.hivemq

import io.micronaut.context.BeanContext
import io.micronaut.mqtt.hivemq.intercept.MqttSubscriberAdvice
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

/*
 * Copyright 2017-2023 original authors
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

@MicronautTest(startApplication = false)
class MicronautMqttEnabledSpec extends Specification {

    @Inject
    BeanContext beanContext

    void "by default mqtt.enabled is enabled"() {
        expect:
        beanContext.containsBean(MqttSubscriberAdvice)
    }

}
