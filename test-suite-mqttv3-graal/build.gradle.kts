plugins {
    id("io.micronaut.build.internal.graal-test")
}

dependencies {
    testImplementation(projects.micronautMqttv3)
    testImplementation(projects.testSuiteUtils)
}
