plugins {
    id("io.micronaut.build.internal.graal-test")
}

dependencies {
    testImplementation(projects.micronautMqttHivemq)
    testImplementation(projects.testSuiteUtils)
    testImplementation(platform(mnTest.boms.testcontainers))
    testImplementation(libs.testcontainers)
}
