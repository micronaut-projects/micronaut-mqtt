/*
 * Copyright 2017-2024 original authors
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
package example.micronaut;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.mqtt.annotation.MqttSubscriber;
import io.micronaut.mqtt.annotation.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@MqttSubscriber
public class SmellListener {

    private static final Logger LOG = LoggerFactory.getLogger(SmellListener.class);
    private Odour smell;

    @Topic("house/livingroom/smell")
    public void receive(Odour data) {
        LOG.info("smell: {}", smell);
        smell = data;
    }

    @NonNull
    public String getSmell() {
        return smell == null ? "" : smell.getName();
    }
}
