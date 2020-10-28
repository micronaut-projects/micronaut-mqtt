package io.micronaut.mqtt.v5.intercept;

import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.AnnotationValue;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.beans.BeanProperty;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.util.StringUtils;
import io.micronaut.mqtt.bind.MqttBinderRegistry;
import io.micronaut.mqtt.bind.MqttBindingContext;
import io.micronaut.mqtt.exception.MqttClientException;
import io.micronaut.mqtt.intercept.AbstractMqttIntroductionAdvice;
import io.micronaut.mqtt.serdes.MqttPayloadSerDesRegistry;
import io.micronaut.mqtt.v5.annotation.MqttClient;
import io.micronaut.mqtt.v5.annotation.MqttProperty;
import io.micronaut.mqtt.v5.bind.MqttV5BindingContext;
import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttActionListener;
import org.eclipse.paho.mqttv5.client.MqttAsyncClient;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Singleton
public class MqttIntroductionAdvice extends AbstractMqttIntroductionAdvice<MqttActionListener, MqttMessage> {

    private final MqttAsyncClient mqttAsyncClient;

    public MqttIntroductionAdvice(MqttAsyncClient mqttAsyncClient,
                                  MqttBinderRegistry binderRegistry) {
        super(binderRegistry);
        this.mqttAsyncClient = mqttAsyncClient;
    }

    @Override
    public Object publish(String topic, MqttMessage message, MqttActionListener listener) {
        try {
            return mqttAsyncClient.publish(topic, message, null, listener);
        } catch (MqttException e) {
            throw new MqttClientException("Failed to publish the message", e);
        }
    }

    @Override
    public MqttBindingContext<MqttMessage> createBindingContext(MethodInvocationContext<Object, Object> context) {
        MqttMessage message = new MqttMessage();
        List<AnnotationValue<MqttProperty>> propertyAnnotations = context.getAnnotationValuesByType(MqttProperty.class);
        Collections.reverse(propertyAnnotations); //set the values in the class first so methods can override
        MqttProperties properties = new MqttProperties();
        propertyAnnotations.forEach((prop) -> {
            String name = prop.get("name", String.class).orElse(null);
            String value = prop.getValue(String.class).orElse(null);
            BeanIntrospection<MqttProperties> introspection = BeanIntrospection.getIntrospection(MqttProperties.class);
            if (StringUtils.isNotEmpty(name) && StringUtils.isNotEmpty(value)) {
                Optional<BeanProperty<MqttProperties, Object>> property = introspection.getProperty(name);
                if (property.isPresent()) {
                    property.get().convertAndSet(properties, value);
                } else {
                    throw new MqttClientException(String.format("Attempted to set property [%s], but could not match the name to any of the properties in %s", name, MqttProperties.class.getName()));
                }
            }
        });
        message.setProperties(properties);
        return new MqttV5BindingContext(message);
    }

    @Override
    public MqttActionListener createListener(Runnable onSuccess, Consumer<Throwable> onError) {
        return new MqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                onSuccess.run();
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                onError.accept(exception);
            }
        };
    }

    @Override
    public Class<? extends Annotation> getRequiredAnnotation() {
        return MqttClient.class;
    }

}
