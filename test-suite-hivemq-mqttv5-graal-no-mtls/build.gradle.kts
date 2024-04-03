plugins {
    id("io.micronaut.build.internal.graal-test")
}

dependencies {
    testImplementation(projects.micronautMqttHivemq)
    testImplementation(mn.snakeyaml)
}

tasks.test {
    systemProperty("javax.net.ssl.trustStore", "$projectDir/mosquitto/certs/ca-cert.jks")
    systemProperty("javax.net.ssl.trustStorePassword", "testtest")
    systemProperty("javax.net.ssl.trustStoreType", "jks")
}

graalvmNative {
    binaries {
        named("test") {
            buildArgs.addAll("-Djavax.net.ssl.trustStore=$projectDir/mosquitto/certs/ca-cert.jks",
                    "-Djavax.net.ssl.trustStorePassword=testtest",
                    "-Djavax.net.ssl.trustStoreType=jks")
        }
    }
}
