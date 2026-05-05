plugins {
    id("io.micronaut.build.internal.mqtt-module")
}

dependencies {
    api(projects.micronautMqttSsl)

    implementation(libs.bcpkix.jdk18on)
    api(libs.managed.hivemq.client)
    testImplementation(platform(mnTest.boms.testcontainers))
    testImplementation(libs.testcontainers)
}
