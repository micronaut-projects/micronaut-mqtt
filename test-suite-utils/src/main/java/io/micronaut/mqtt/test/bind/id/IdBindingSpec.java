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
package io.micronaut.mqtt.test.bind.id;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import io.micronaut.mqtt.annotation.Id;
import io.micronaut.mqtt.annotation.MqttSubscriber;
import io.micronaut.mqtt.annotation.Topic;
import io.micronaut.mqtt.test.AbstractMQTTTest;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.awaitility.Awaitility.await;
import static java.time.Duration.ofSeconds;

public abstract class IdBindingSpec implements AbstractMQTTTest {

    @Test
    void testIdBinding() {
        Map<String, Object> config = new HashMap<>();
        config.put("idbindingspec", true);
        ApplicationContext ctx = startContext(config);
        IdBindingClient client = ctx.getBean(getClient());
        MySubscriber sub = ctx.getBean(MySubscriber.class);

        client.send();
        client.send2();

        await().atMost(ofSeconds(3)).untilAsserted(() -> {
            assertNotNull(sub.id1);
            assertNotNull(sub.id2);
        });

        ctx.close();
    }

    public abstract Class<? extends IdBindingClient> getClient();

    @Requires(property = "idbindingspec", value = StringUtils.TRUE)
    @MqttSubscriber
    public static class MySubscriber {

        public Integer id1 = null;
        public Integer id2 = null;

        @Topic("test/id/1")
        public void get1(@Id Integer id) {
            this.id1 = id;
        }

        @Topic("test/id/2")
        public void get2(Integer id) {
            this.id2 = id;
        }
    }
}