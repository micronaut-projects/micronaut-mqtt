/*
 * Copyright 2017-2020 original authors
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

import io.micronaut.core.bind.annotation.Bindable;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArrayUtils;

import javax.inject.Singleton;
import java.lang.annotation.Annotation;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A default implementation of {@link MqttBinderRegistry} that searches
 * for binders based on argument annotations, then based on the argument
 * type.
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@Singleton
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
    public <T> MqttBinder<?, T> findArgumentBinder(Argument<T> argument) {
        Optional<Class<? extends Annotation>> opt = argument.getAnnotationMetadata().getAnnotationTypeByStereotype(Bindable.class);
        if (opt.isPresent()) {
            Class<? extends Annotation> annotationType = opt.get();
            MqttBinder binder = byAnnotation.get(annotationType);
            if (binder != null) {
                return binder;
            }
        } else {
            MqttBinder binder = byType.get(argument.typeHashCode());
            if (binder != null) {
                return binder;
            }
        }
        return (MqttBinder<?, T>) fallbackMqttBinder;
    }
}
