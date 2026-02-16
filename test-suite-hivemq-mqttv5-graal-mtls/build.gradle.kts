plugins {
    id("io.micronaut.build.internal.graal-test")
}

dependencies {
    testImplementation(projects.micronautMqttHivemq)
    testImplementation(mn.snakeyaml)
    testImplementation(platform(mnTest.boms.testcontainers))
    testImplementation(libs.testcontainers)
}
