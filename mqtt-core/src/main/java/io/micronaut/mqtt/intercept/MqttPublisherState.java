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
package io.micronaut.mqtt.intercept;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.type.Argument;
import io.micronaut.mqtt.bind.MqttBinder;

@Internal
final class MqttPublisherState {

    private String topic;
    private Integer qos;
    private Boolean retained;

    // by position, not by argument: the state is cached by method equality, and an equal method, such as one of
    // another instance of the publisher, can have other argument instances
    private MqttBinder<Object, Object>[] binders;

    String getTopic() {
        return topic;
    }

    Integer getQos() {
        return qos;
    }

    Boolean getRetained() {
        return retained;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public void setQos(Integer qos) {
        this.qos = qos;
    }

    public void setRetained(Boolean retained) {
        this.retained = retained;
    }

    public void setBinders(MqttBinder<Object, Object>[] binders) {
        this.binders = binders;
    }

    public void bind(Object message, MethodInvocationContext<Object, Object> context) {
        Argument<?>[] arguments = context.getArguments();
        Object[] parameterValues = context.getParameterValues();
        for (int i = 0; i < arguments.length; i++) {
            MqttBinder<Object, Object> binder = binders[i];
            if (binder != null) {
                binder.bindTo(message, parameterValues[i], (Argument) arguments[i]);
            }
        }
    }
}
