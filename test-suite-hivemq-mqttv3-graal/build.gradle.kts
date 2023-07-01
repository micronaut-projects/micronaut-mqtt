plugins {
    id("io.micronaut.build.internal.graal-test")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(projects.micronautMqttHivemq)
}
