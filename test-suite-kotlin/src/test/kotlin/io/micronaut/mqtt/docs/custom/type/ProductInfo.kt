package io.micronaut.mqtt.docs.custom.type

// tag::clazz[]
class ProductInfo(val size: String?, // <1>
                  val count: Long, // <2>
                  val sealed: Boolean)// <3>
// end::clazz[]
