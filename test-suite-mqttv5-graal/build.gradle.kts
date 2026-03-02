plugins {
    id("io.micronaut.build.internal.graal-test")
}

dependencies {
    testImplementation(projects.micronautMqttv5)
    testImplementation(projects.testSuiteUtils)
}

