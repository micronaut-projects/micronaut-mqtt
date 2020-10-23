package io.micronaut.mqtt.intercept;

import io.micronaut.context.processor.ExecutableMethodProcessor;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.ExecutableMethod;
import io.micronaut.mqtt.annotation.MqttSubscriber;
import io.micronaut.mqtt.annotation.Topic;

public abstract class AbstractMqttSubscriberAdvice implements ExecutableMethodProcessor<MqttSubscriber>, AutoCloseable {


    @Override
    public void process(BeanDefinition<?> beanDefinition, ExecutableMethod<?, ?> method) {
        AnnotationValue<Topic> topicAnn = method.getAnnotation(Topic.class);
        if (topicAnn != null) {

        }
    }

    @Override
    public void close() throws Exception {

    }
}
