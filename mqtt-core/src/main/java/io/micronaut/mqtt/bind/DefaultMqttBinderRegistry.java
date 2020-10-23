package io.micronaut.mqtt.bind;

import io.micronaut.core.bind.ArgumentBinder;
import io.micronaut.core.bind.annotation.Bindable;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArrayUtils;

import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class DefaultMqttBinderRegistry implements MqttBinderRegistry {

    private final Map<Class<? extends Annotation>, MqttBinder<?, ?>> byAnnotation = new LinkedHashMap<>();
    private final Map<Integer, MqttBinder<?, ?>> byType = new LinkedHashMap<>();
    private final FallbackMqttBinder<?> fallbackMqttBinder;

    /**
     * Default constructor.
     *
     * @param fallbackMqttBinder The binder to use when one cannot be found for an argument
     * @param binders The list of binders to choose from to bind an argument
     */
    public DefaultMqttBinderRegistry(FallbackMqttBinder<?> fallbackMqttBinder,
                                     MqttBinder<?, ?>... binders) {
        this.fallbackMqttBinder = fallbackMqttBinder;
        if (ArrayUtils.isNotEmpty(binders)) {
            for (MqttBinder binder : binders) {
                if (binder instanceof AnnotatedMqttBinder) {
                    AnnotatedMqttBinder<?, ?> annotatedBinder = (AnnotatedMqttBinder<?, ?>) binder;
                    byAnnotation.put(
                            annotatedBinder.getAnnotationType(),
                            binder
                    );
                } else if (binder instanceof TypedMqttBinder) {
                    TypedMqttBinder<?, ?> typedBinder = (TypedMqttBinder<?, ?>) binder;
                    byType.put(
                            typedBinder.getArgumentType().typeHashCode(),
                            typedBinder
                    );
                }
            }
        }
    }

    @Override
    public <T> Optional<MqttBinder<?, T>> findArgumentBinder(Argument<T> argument) {
        Optional<Class<? extends Annotation>> opt = argument.getAnnotationMetadata().getAnnotationTypeByStereotype(Bindable.class);
        if (opt.isPresent()) {
            Class<? extends Annotation> annotationType = opt.get();
            MqttBinder binder = byAnnotation.get(annotationType);
            if (binder != null) {
                return Optional.of(binder);
            }
        } else {
            MqttBinder binder = byType.get(argument.typeHashCode());
            if (binder != null) {
                return Optional.of(binder);
            }
        }
        return Optional.of((MqttBinder<?, T>) fallbackMqttBinder);
    }
}
