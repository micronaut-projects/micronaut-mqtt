/*
 * Copyright 2017-2022 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.mqtt.test.intercept;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import io.micronaut.mqtt.annotation.MqttSubscriber;
import io.micronaut.mqtt.annotation.Topic;
import io.micronaut.mqtt.test.AbstractMQTTTest;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.awaitility.Awaitility.await;
import static java.time.Duration.ofSeconds;

public abstract class MultipleTopicsSpec implements AbstractMQTTTest {

    @Test
    @Disabled
    void testSubscribingToMultipleTopics() {
        Map<String, Object> config = new HashMap<>();
        config.put("multipletopicstest", true);
        ApplicationContext ctx = startContext(config);
        MultipleTopicsClient client = ctx.getBean(getClient());
        MySubscriber subscriber = ctx.getBean(MySubscriber.class);
        MySubscriber2 subscriber2 = ctx.getBean(MySubscriber2.class);

        client.override();
        await().atMost(ofSeconds(3)).untilAsserted(() -> {
            assertEquals(1, subscriber.topics.size());
            assertEquals("test/topic1", subscriber.topics.get(0));
        });

        client.send("test/topic2");
        await().atMost(ofSeconds(3)).untilAsserted(() -> {
            assertEquals(2, subscriber.topics.size());
            assertEquals("test/topic2", subscriber.topics.get(1));
        });

        client.send("test/topic/classlevel/1");
        await().atMost(ofSeconds(3)).untilAsserted(() -> {
            assertEquals(1, subscriber2.topics.size());
            assertEquals("test/topic/classlevel/1", subscriber2.topics.get(0));
        });

        client.send("test/topic/classlevel/2");
        await().atMost(ofSeconds(3)).untilAsserted(() -> {
            assertEquals(2, subscriber2.topics.size());
            assertEquals("test/topic/classlevel/2", subscriber2.topics.get(1));
        });

        ctx.close();
    }

    public abstract Class<? extends MultipleTopicsClient> getClient();

    @Requires(property = "multipletopicstest", value = StringUtils.TRUE)
    @MqttSubscriber
    public static class MySubscriber {

        public List<String> topics = new ArrayList<>();

        @Topic("test/topic1")
        @Topic("test/topic2")
        public void get(String topic) {
            topics.add(topic);
        }
    }

    @Requires(property = "multipletopicstest", value = StringUtils.TRUE)
    @Topic("test/topic/classlevel/1")
    @MqttSubscriber
    public static class MySubscriber2 {

        public List<String> topics = new ArrayList<>();

        @Topic("test/topic/classlevel/2")
        public void get(String topic) {
            topics.add(topic);
        }
    }
}