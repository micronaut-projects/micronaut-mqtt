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
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.awaitility.Awaitility.await;
import static java.time.Duration.ofSeconds;

public abstract class SimplePubSubSpec implements AbstractMQTTTest {

    @Test
    void testSimplePublishingAndSubscribing() {
        Map<String, Object> config = new HashMap<>();
        config.put("simplepubsubtest", true);
        ApplicationContext ctx = startContext(config);
        SimplePubSubClient client = ctx.getBean(getClient());
        MySubscriber subscriber = ctx.getBean(MySubscriber.class);

        client.publish("test body");

        await().atMost(ofSeconds(3)).untilAsserted(() -> {
            assertEquals("test body", subscriber.payload);
        });

        ctx.close();
    }

    public abstract Class<? extends SimplePubSubClient> getClient();

    @Requires(property = "simplepubsubtest", value = StringUtils.TRUE)
    @MqttSubscriber
    public static class MySubscriber {

        public String payload;

        @Topic("test/simple")
        public void get(String payload) {
            this.payload = payload;
        }
    }
}