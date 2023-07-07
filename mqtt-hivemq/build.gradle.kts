plugins {
    id("io.micronaut.build.internal.mqtt-module")
}

dependencies {
    api(projects.micronautMqttSsl)

    implementation(libs.bcpkix.jdk15on)
    api(libs.managed.hivemq.client)
}

micronautBuild {
    binaryCompatibility {
        enabled.set(false)
    }
}
