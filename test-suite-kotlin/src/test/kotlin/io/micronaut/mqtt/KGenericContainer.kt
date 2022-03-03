package io.micronaut.mqtt

import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName

class KGenericContainer(imageName: String) : GenericContainer<KGenericContainer>(DockerImageName.parse(imageName))
