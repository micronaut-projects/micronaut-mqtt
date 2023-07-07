import io.micronaut.testresources.buildtools.KnownModules.HIVEMQ

plugins {
    id("io.micronaut.build.internal.graal-test")
}

dependencies {
    testImplementation(projects.micronautMqttHivemq)
}

micronaut {
    testResources {
        additionalModules.add(HIVEMQ)
    }
}
