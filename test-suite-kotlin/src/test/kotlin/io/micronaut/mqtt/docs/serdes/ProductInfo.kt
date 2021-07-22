package io.micronaut.mqtt.docs.serdes

// tag::clazz[]
class ProductInfo(val size: String?, // <1>
                  val count: Long, // <2>
                  val sealed: Boolean)// <3>
// end::clazz[]
