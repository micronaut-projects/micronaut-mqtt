package io.micronaut.mqtt.docs

import io.micronaut.runtime.Micronaut.*
fun main(args: Array<String>) {
    build()
            .args(*args)
            .packages("com.example")
            .start()
}
