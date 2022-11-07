package io.micronaut.mqtt.docs.custom.type;

// tag::imports[]
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.mqtt.bind.TypedMqttBinder;
import io.micronaut.mqtt.v5.bind.MqttV5BindingContext;
import jakarta.inject.Singleton;
import org.eclipse.paho.mqttv5.common.packet.UserProperty;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
// end::imports[]

import io.micronaut.context.annotation.Requires;

@Requires(property = "spec.name", value = "ProductInfoSpec")
// tag::clazz[]
@Singleton // <1>
public class ProductInfoTypeBinder implements TypedMqttBinder<MqttV5BindingContext, ProductInfo> { //<2>

    private final ConversionService conversionService;

    ProductInfoTypeBinder(ConversionService conversionService) { //<3>
        this.conversionService = conversionService;
    }

    @Override
    public Argument<ProductInfo> getArgumentType() {
        return Argument.of(ProductInfo.class);
    }

    @Override
    public void bindTo(MqttV5BindingContext context, ProductInfo value, Argument<ProductInfo> argument) {
        List<UserProperty> userPropertiesList = context.getProperties().getUserProperties();
        if (value.getSize() != null) {
            userPropertiesList.add(new UserProperty("productSize", value.getSize()));
        }
        userPropertiesList.add(new UserProperty("productCount", value.getCount().toString())); // <4>
        userPropertiesList.add(new UserProperty("productSealed", value.getSealed().toString()));
    }

    @Override
    public Optional<ProductInfo> bindFrom(MqttV5BindingContext context, ArgumentConversionContext<ProductInfo> conversionContext) {
        List<UserProperty> userPropertiesList = context.getProperties().getUserProperties();
        Map<String, String> userProperties = userPropertiesList.stream()
                .collect(Collectors.toMap(UserProperty::getKey, UserProperty::getValue));
        String size = userProperties.get("productSize");
        Optional<Long> count = Optional.ofNullable(userProperties.get("productCount"))
                .flatMap(value -> conversionService.convert(value, Long.class));
        Optional<Boolean> sealed = Optional.ofNullable(userProperties.get("productSealed"))
                .flatMap(value -> conversionService.convert(value, Boolean.class));

        if (count.isPresent() && sealed.isPresent()) {
            return Optional.of(new ProductInfo(size, count.get(), sealed.get())); // <5>
        } else {
            return Optional.empty();
        }
    }
}
// end::clazz[]
