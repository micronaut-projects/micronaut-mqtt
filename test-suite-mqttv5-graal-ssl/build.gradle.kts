plugins {
    id("io.micronaut.build.internal.graal-test")
}

dependencies {
    testImplementation(projects.micronautMqttCore)
    testImplementation(projects.micronautMqttv5)
    testImplementation(projects.micronautMqttSsl)
    testImplementation(mn.snakeyaml)
    testImplementation(platform{mnTest.boms.testcontainers})
    testImplementation(libs.testcontainers)
}
