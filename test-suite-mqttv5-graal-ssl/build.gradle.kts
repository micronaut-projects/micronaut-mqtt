plugins {
    id("io.micronaut.build.internal.graal-test")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(projects.micronautMqttv5)
    runtimeOnly(mn.snakeyaml)
}
