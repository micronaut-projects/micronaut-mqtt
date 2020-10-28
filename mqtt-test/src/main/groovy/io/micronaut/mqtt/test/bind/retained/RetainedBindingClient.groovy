package io.micronaut.mqtt.test.bind.retained

import io.micronaut.mqtt.annotation.Retained
import io.micronaut.mqtt.annotation.Topic

@Topic("test/retained")
@Retained(false)
interface RetainedBindingClient {

    void argument(@Retained Boolean retained)

    @Retained(true)
    void override()

    void classLevel()
}
