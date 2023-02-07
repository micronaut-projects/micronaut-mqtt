import java.util.Locale
import io.micronaut.testresources.buildtools.KnownModules

plugins {
    id("org.graalvm.buildtools.native")
    id("io.micronaut.application") version "3.7.0"
    id("io.micronaut.test-resources") version "3.7.0"
}

repositories {
    mavenCentral()
}

dependencies {
    testAnnotationProcessor(mn.micronaut.inject.java)
    testImplementation(mn.micronaut.http)
    testImplementation(mn.micronaut.json.core)
    testImplementation(mn.micronaut.http.client)
    testImplementation(mn.micronaut.http.server.netty)
    testImplementation(mn.micronaut.test.junit5)

    testImplementation(libs.awaitility)

    testImplementation(projects.mqttv3)

    testRuntimeOnly(mn.logback)
}

micronaut {
    testResources {
        additionalModules.add(KnownModules.HIVEMQ)
    }
}

tasks {
    test {
        useJUnitPlatform()
    }

    named("check") {
        if (listOf("jvmci.Compiler", "java.vendor.version", "java.vendor").any {
                System.getProperty(it)?.toLowerCase(Locale.ENGLISH)?.contains("graal") == true
            }) {
            dependsOn("nativeTest")
        }
    }
}

val openGraalModules = listOf(
    "org.graalvm.nativeimage.builder/com.oracle.svm.core.jdk",
    "org.graalvm.nativeimage.builder/com.oracle.svm.core.configure",
    "org.graalvm.sdk/org.graalvm.nativeimage.impl"
)

graalvmNative {
    toolchainDetection.set(false)
    binaries {
        all {
            buildArgs.add("-H:-UseServiceLoaderFeature")
            openGraalModules.forEach { module ->
                jvmArgs.add("--add-exports=" + module + "=ALL-UNNAMED")
            }
        }
    }
}
