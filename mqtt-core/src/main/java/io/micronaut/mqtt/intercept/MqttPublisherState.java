package io.micronaut.mqtt.intercept;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.type.Argument;
import io.micronaut.mqtt.bind.MqttBinder;

import java.util.IdentityHashMap;
import java.util.Map;

public class MqttPublisherState {

    private String topic;
    private Integer qos;
    private Boolean retained;

    private Map<Argument<?>, MqttBinder<Object, Object>> binderCache = new IdentityHashMap<>(5);

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

    public void setBinder(Argument<?> argument, MqttBinder<Object, Object> binder) {
        binderCache.put(argument, binder);
    }

    public void bind(Object message, MethodInvocationContext<Object, Object> context) {
        Map<String, Object> parameterValues = context.getParameterValueMap();
        for (Argument<?> argument: context.getArguments()) {
            MqttBinder<Object, Object> binder = binderCache.get(argument);
            if (binder != null) {
                binder.bindTo(message, parameterValues.get(argument.getName()), argument);
            }
        }
    }
}
