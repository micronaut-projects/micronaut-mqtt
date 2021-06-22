package io.micronaut.mqtt.docs.serdes;

// tag::imports[]
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.mqtt.serdes.MqttPayloadSerDes;
import jakarta.inject.Singleton;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

// end::imports[]
import io.micronaut.context.annotation.Requires;

@Requires(property = "spec.name", value = "ProductInfoSerDesSpec")
// tag::clazz[]
@Singleton // <1>
public class ProductInfoSerDes implements MqttPayloadSerDes<ProductInfo> { // <2>

    private static final Charset CHARSET = StandardCharsets.UTF_8;

    private final ConversionService<?> conversionService;

    public ProductInfoSerDes(ConversionService<?> conversionService) { // <3>
        this.conversionService = conversionService;
    }

    @Nullable
    @Override
    public ProductInfo deserialize(byte[] payload, Argument<ProductInfo> argument) {
        String body = new String(payload, CHARSET);
        String[] parts = body.split("\\|");
        if (parts.length == 3) {
            String size = parts[0];
            if (size.equals("null")) {
                size = null;
            }

            Optional<Long> count = conversionService.convert(parts[1], Long.class);
            Optional<Boolean> sealed = conversionService.convert(parts[2], Boolean.class);

            if (count.isPresent() && sealed.isPresent()) {
                return new ProductInfo(size, count.get(), sealed.get()); // <4>
            }
        }
        return null;
    }

    @Nullable
    @Override
    public byte[] serialize(@Nullable ProductInfo data) {
        if (data == null) {
            return null;
        }
        return (data.getSize() + "|" + data.getCount() + "|" + data.getSealed()).getBytes(CHARSET); // <5>
    }

    @Override
    public boolean supports(Argument<ProductInfo> argument) { // <6>
        return argument.getType().isAssignableFrom(ProductInfo.class);
    }
}
// end::clazz[]
