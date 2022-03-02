package io.micronaut.mqtt.docs.custom.type

// tag::imports[]
import io.micronaut.core.convert.ArgumentConversionContext
import io.micronaut.core.convert.ConversionService
import io.micronaut.core.type.Argument
import io.micronaut.mqtt.bind.*
import io.micronaut.mqtt.v5.bind.MqttV5BindingContext
import jakarta.inject.Singleton
import org.eclipse.paho.mqttv5.common.packet.UserProperty

import java.util.Optional
import java.util.stream.Collectors
// end::imports[]

import io.micronaut.context.annotation.Requires

@Requires(property = "spec.name", value = "ProductInfoSpec")
// tag::clazz[]
@Singleton // <1>
class ProductInfoTypeBinder constructor(private val conversionService: ConversionService<*>) //<3>
    : TypedMqttBinder<MqttV5BindingContext, ProductInfo> { // <2>

    override fun bindTo(context: MqttV5BindingContext, value: ProductInfo, argument: Argument<ProductInfo>) {
        val userPropertiesList = context.properties.userProperties
        if (value.size != null) {
            userPropertiesList.add(UserProperty("productSize", value.size)) // <4>
        }
        userPropertiesList.add(UserProperty("productCount", value.count.toString()))
        userPropertiesList.add(UserProperty("productSealed", value.sealed.toString()))
    }

    override fun bindFrom(context: MqttV5BindingContext, conversionContext: ArgumentConversionContext<ProductInfo>): Optional<ProductInfo> {
        val userPropertiesList = context.properties.userProperties
        val userProperties = userPropertiesList.stream()
                .collect(Collectors.toMap(UserProperty::getKey, UserProperty::getValue))
        val size = userProperties.get("productSize")
        val count = Optional.ofNullable(userProperties.get("productCount"))
                .flatMap { value -> conversionService.convert(value, Long::class.java) }
        val sealed = Optional.ofNullable(userProperties.get("productSealed"))
                .flatMap { value -> conversionService.convert(value, Boolean::class.java) }

        if (count.isPresent && sealed.isPresent) {
            return Optional.of(ProductInfo(size, count.get(), sealed.get())) // <5>
        } else {
            return Optional.empty()
        }
    }

    override fun getArgumentType(): Argument<ProductInfo> {
        return Argument.of(ProductInfo::class.java)
    }

}
// end::clazz[]
