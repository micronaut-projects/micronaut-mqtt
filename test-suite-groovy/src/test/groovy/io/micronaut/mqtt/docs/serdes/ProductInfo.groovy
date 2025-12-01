package io.micronaut.mqtt.docs.serdes

// tag::clazz[]
import org.jspecify.annotations.NonNull
import org.jspecify.annotations.Nullable

class ProductInfo {

    private String size
    private Long count
    private Boolean sealed

    ProductInfo(@Nullable String size, // <1>
                @NonNull Long count, // <2>
                @NonNull Boolean sealed) { // <3>
        this.size = size
        this.count = count
        this.sealed = sealed
    }

    String getSize() {
        size
    }

    Long getCount() {
        count
    }

    Boolean getSealed() {
        sealed
    }
}
// end::clazz[]
