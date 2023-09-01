plugins {
    id("groovy-gradle-plugin")
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation("org.graalvm.buildtools.native:org.graalvm.buildtools.native.gradle.plugin:0.9.25")
    implementation("io.micronaut.gradle:micronaut-gradle-plugin:4.0.3")
    implementation("io.micronaut.gradle:micronaut-test-resources-plugin:4.0.0-M8")
}
