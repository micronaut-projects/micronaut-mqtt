plugins {
    id("groovy-gradle-plugin")
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation("org.graalvm.buildtools.native:org.graalvm.buildtools.native.gradle.plugin:0.10.3")
    implementation (libs.gradle.kotlin)
    implementation (libs.gradle.kotlin.allopen)
    implementation (libs.gradle.kotlin.noarg)
    implementation (libs.micronaut.gradle.plugin)
}
