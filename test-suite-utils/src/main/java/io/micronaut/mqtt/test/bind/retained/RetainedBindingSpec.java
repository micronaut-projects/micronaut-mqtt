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
package io.micronaut.mqtt.test.bind.retained;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import io.micronaut.messaging.annotation.MessageBody;
import io.micronaut.mqtt.annotation.MqttSubscriber;
import io.micronaut.mqtt.annotation.Topic;
import io.micronaut.mqtt.test.AbstractMQTTTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import jakarta.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.awaitility.Awaitility.await;
import static java.time.Duration.ofSeconds;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class RetainedBindingSpec implements AbstractMQTTTest {

    @Test
    @Order(1)
    void testRetainedBindingClassValueIsUsed() {
        ApplicationContext ctx = startContext();
        RetainedBindingClient client = ctx.getBean(getClient());
        client.classLevel("classLevel");
        ctx.close();

        Map<String, Object> config = new HashMap<>();
        config.put("retainedbindingspec", true);
        ctx = startContext(config);
        MySubscriber sub = ctx.getBean(MySubscriber.class);

        await().pollDelay(ofSeconds(3)).atMost(ofSeconds(5)).untilAsserted(() -> {
            assertNull(sub.payload);
        });

        ctx.close();
    }

    @Test
    @Order(2)
    void testRetainedBindingArgumentSetToFalse() {
        ApplicationContext ctx = startContext();
        RetainedBindingClient client = ctx.getBean(getClient());
        client.argument(false, "argumentFalse");
        ctx.close();

        Map<String, Object> config = new HashMap<>();
        config.put("retainedbindingspec", true);
        ctx = startContext(config);
        MySubscriber sub = ctx.getBean(MySubscriber.class);

        await().pollDelay(ofSeconds(3)).atMost(ofSeconds(5)).untilAsserted(() -> {
            assertNull(sub.payload);
        });

        ctx.close();
    }

    @Test
    @Order(3)
    void testRetainedBindingMethodOverridesClass() {
        ApplicationContext ctx = startContext();
        RetainedBindingClient client = ctx.getBean(getClient());
        client.override("overrides");
        ctx.close();

        Map<String, Object> config = new HashMap<>();
        config.put("retainedbindingspec", true);
        ctx = startContext(config);
        MySubscriber sub = ctx.getBean(MySubscriber.class);

        await().atMost(ofSeconds(3)).untilAsserted(() -> {
            assertEquals("overrides", sub.payload);
        });

        ctx.close();
    }

    @Test
    @Order(4)
    void testRetainedBindingArgumentSetToTrue() {
        ApplicationContext ctx = startContext();
        RetainedBindingClient client = ctx.getBean(getClient());
        client.argument(true, "argumentTrue");

        Map<String, Object> config = new HashMap<>();
        config.put("retainedbindingspec", true);
        ApplicationContext ctx2 = startContext(config);
        MySubscriber sub = ctx2.getBean(MySubscriber.class);

        await().atMost(ofSeconds(3)).untilAsserted(() -> {
            assertEquals("argumentTrue", sub.payload);
        });

        client.argument(true, ""); // clean up retained message for subsequent tests
        ctx.close();
        ctx2.close();
    }

    public abstract Class<? extends RetainedBindingClient> getClient();

    @Requires(property = "retainedbindingspec", value = StringUtils.TRUE)
    @MqttSubscriber
    public static class MySubscriber {

        public String payload = null;

        @Topic("test/retained")
        public void get(@Nullable @MessageBody String payload) {
            this.payload = payload;
        }
    }
}