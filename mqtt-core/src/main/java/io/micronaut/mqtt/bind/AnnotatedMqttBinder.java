package io.micronaut.mqtt.bind;

import java.lang.annotation.Annotation;

public interface AnnotatedMqttBinder<M, T extends Annotation> extends MqttBinder<M, Object> {

    Class<T> getAnnotationType();

}
