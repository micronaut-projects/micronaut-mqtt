package io.micronaut.mqtt.docs.custom.type

// tag::clazz[]
import org.jspecify.annotations.NonNull
import org.jspecify.annotations.Nullable

class ProductInfo {

    private String size
    private Long count
    private Boolean seal

    ProductInfo(@Nullable String size, // <1>
                @NonNull Long count, // <2>
                @NonNull Boolean seal) { // <3>
        this.size = size
        this.count = count
        this.seal = seal
    }

    String getSize() {
        size
    }

    Long getCount() {
        count
    }

    Boolean getSealed() {
        seal
    }
}
// end::clazz[]
