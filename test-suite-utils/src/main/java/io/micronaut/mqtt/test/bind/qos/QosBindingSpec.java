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
package io.micronaut.mqtt.test.bind.qos;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import io.micronaut.mqtt.annotation.MqttSubscriber;
import io.micronaut.mqtt.annotation.Qos;
import io.micronaut.mqtt.annotation.Topic;
import io.micronaut.mqtt.test.AbstractMQTTTest;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.awaitility.Awaitility.await;
import static java.time.Duration.ofSeconds;

public abstract class QosBindingSpec implements AbstractMQTTTest {

    @Test
    void testQosBinding() {
        Map<String, Object> config = new HashMap<>();
        config.put("qosbindingspec", true);
        ApplicationContext ctx = startContext(config);
        QosBindingClient client = ctx.getBean(getClient());
        MySubscriber sub = ctx.getBean(MySubscriber.class);

        client.override();
        await().atMost(ofSeconds(3)).untilAsserted(() -> {
            assertEquals(2, sub.qos);
        });

        client.argument(1);
        await().atMost(ofSeconds(3)).untilAsserted(() -> {
            assertEquals(1, sub.qos);
        });

        client.classLevel();
        await().atMost(ofSeconds(3)).untilAsserted(() -> {
            assertEquals(0, sub.qos);
        });

        ctx.close();
    }

    public abstract Class<? extends QosBindingClient> getClient();

    @Requires(property = "qosbindingspec", value = StringUtils.TRUE)
    @MqttSubscriber
    public static class MySubscriber {

        public Integer qos = null;

        @Topic(value = "test/qos", qos = 2)
        public void get(@Qos Integer qos) {
            this.qos = qos;
        }
    }
}