package io.micronaut.mqtt.test.bind.topic

import io.micronaut.mqtt.annotation.Topic

@Topic("test/topic/classlevel")
interface TopicBindingClient {

    void argument(@Topic String topic)

    @Topic("test/topic/override")
    void override()

    void classLevelTopic()
}
