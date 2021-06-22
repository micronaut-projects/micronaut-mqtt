package io.micronaut.mqtt.docs.serdes

import io.micronaut.mqtt.serdes.MqttPayloadSerDes

// tag::imports[]
import io.micronaut.core.convert.ConversionService
import io.micronaut.core.type.Argument
import jakarta.inject.Singleton
import org.jetbrains.annotations.Nullable

import java.nio.charset.Charset
// end::imports[]
import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "ProductInfoSerDesSpec")
// tag::clazz[]
@Singleton // <1>
class ProductInfoSerDes implements MqttPayloadSerDes<ProductInfo> { // <2>

    private static final Charset CHARSET = Charset.forName("UTF-8")

    private final ConversionService conversionService

    ProductInfoSerDes(ConversionService conversionService) { // <3>
        this.conversionService = conversionService
    }

    @Nullable
    @Override
    ProductInfo deserialize(byte[] payload, Argument<ProductInfo> argument) {
        String body = new String(payload, CHARSET)
        String[] parts = body.split("\\|")
        if (parts.length == 3) {
            String size = parts[0]
            if (size == "null") {
                size = null
            }

            Optional<Long> count = conversionService.convert(parts[1], Long.class)
            Optional<Boolean> sealed = conversionService.convert(parts[2], Boolean.class)

            if (count.isPresent() && sealed.isPresent()) {
                return new ProductInfo(size, count.get(), sealed.get()) // <4>
            }
        }
        return null
    }

    @Nullable
    @Override
    byte[] serialize(@Nullable ProductInfo data) {
        if (data == null) {
            return null
        }
        return "${data.size}|${data.count}|${data.sealed}".getBytes(CHARSET) // <5>
    }

    @Override
    boolean supports(Argument<ProductInfo> argument) { // <6>
        argument.type.isAssignableFrom(ProductInfo)
    }
}
// end::clazz[]
