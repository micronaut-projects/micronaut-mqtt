package io.micronaut.mqtt.docs.serdes

// tag::imports[]
import io.micronaut.context.annotation.Requires
import io.micronaut.core.convert.ConversionService
import io.micronaut.core.type.Argument
import io.micronaut.mqtt.serdes.MqttPayloadSerDes

import javax.inject.Singleton
import java.nio.charset.Charset
// end::imports[]

@Requires(property = "spec.name", value = "ProductInfoSerDesSpec")
// tag::clazz[]
@Singleton // <1>
class ProductInfoSerDes(private val conversionService: ConversionService<*>)// <3>
    : MqttPayloadSerDes<ProductInfo> { // <2>

    override fun deserialize(payload: ByteArray, argument: Argument<ProductInfo>): ProductInfo? {
        val body = String(payload, CHARSET)
        val parts = body.split("\\|".toRegex())
        if (parts.size == 3) {
            var size: String? = parts[0]
            if (size == "null") {
                size = null
            }

            val count = conversionService.convert(parts[1], Long::class.java)
            val sealed = conversionService.convert(parts[2], Boolean::class.java)

            if (count.isPresent && sealed.isPresent) {
                return ProductInfo(size, count.get(), sealed.get())
            }
        }
        return null
    }

    override fun serialize(data: ProductInfo?): ByteArray {
        return (data?.size + "|" + data?.count + "|" + data?.sealed).toByteArray(CHARSET)
    }

    override fun supports(argument: Argument<ProductInfo>): Boolean { // <6>
        return argument.type.isAssignableFrom(ProductInfo::class.java)
    }

    companion object {
        private val CHARSET = Charset.forName("UTF-8")
    }

}
// end::clazz[]
