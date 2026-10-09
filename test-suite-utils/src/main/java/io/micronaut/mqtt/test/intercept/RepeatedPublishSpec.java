/*
 * Copyright 2017-2026 original authors
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
import io.micronaut.mqtt.HiveMQ;
import io.micronaut.mqtt.test.AbstractMQTTTest;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.time.Duration.ofSeconds;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

/**
 * Every call of a publisher method publishes its own payload, not only the first, and so does every instance of a
 * publisher.
 */
public abstract class RepeatedPublishSpec implements AbstractMQTTTest {

    public static final String TOPIC = "test/repeated";

    @Test
    void testEveryCallOfAPublisherMethodPublishesItsPayload() {
        try (ApplicationContext ctx = startContext(config())) {
            RepeatedPublishClient client = ctx.getBean(getClient());
            RepeatedSubscriber subscriber = ctx.getBean(getSubscriber());

            client.publish("one");
            client.publish("two");
            client.publish(TOPIC, "three".getBytes(StandardCharsets.UTF_8));
            client.publish(TOPIC, "four".getBytes(StandardCharsets.UTF_8));

            await().atMost(ofSeconds(5)).untilAsserted(() ->
                assertEquals(List.of("one", "two", "three", "four"), subscriber.payloads));
        }
    }

    @Test
    void testEveryInstanceOfAPublisherPublishesItsPayload() {
        try (ApplicationContext ctx = startContext(config())) {
            PrototypePublishClient first = ctx.getBean(getPrototypeClient());
            PrototypePublishClient second = ctx.getBean(getPrototypeClient());
            assertNotSame(first, second);
            RepeatedSubscriber subscriber = ctx.getBean(getSubscriber());

            first.publish("one");
            second.publish("two");
            first.publish("three");

            await().atMost(ofSeconds(5)).untilAsserted(() ->
                assertEquals(List.of("one", "two", "three"), subscriber.payloads));
        }
    }

    private static Map<String, Object> config() {
        Map<String, Object> config = new HashMap<>(HiveMQ.getProperties());
        config.put("mqtt.client.client-id", UUID.randomUUID().toString());
        return config;
    }

    public abstract Class<? extends RepeatedPublishClient> getClient();

    /**
     * @return A publisher of prototype scope, of which each instance has its own executable methods
     */
    public abstract Class<? extends PrototypePublishClient> getPrototypeClient();

    public abstract Class<? extends RepeatedSubscriber> getSubscriber();

    /**
     * Records the payloads received on {@link #TOPIC}.
     */
    public abstract static class RepeatedSubscriber {

        private final List<String> payloads = new CopyOnWriteArrayList<>();

        protected void receive(String payload) {
            payloads.add(payload);
        }
    }
}
