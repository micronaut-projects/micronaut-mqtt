plugins {
    id("groovy-gradle-plugin")
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation("org.graalvm.buildtools.native:org.graalvm.buildtools.native.gradle.plugin:0.9.14")
    implementation("io.micronaut.gradle:micronaut-gradle-plugin:3.7.0")
    implementation("io.micronaut.gradle:micronaut-test-resources-plugin:3.7.0")
}
