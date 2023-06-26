/*
 * Copyright 2017-2023 original authors
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
package io.micronaut.mqtt.hivemq.bind;

/**
 * Represents a MQTT v5 property.
 *
 * @author Sven Kobow
 * @since 3.0.0
 */
public class UserProperty {
    private final String key;
    private final String value;

    public UserProperty(String key, String value) {
        this.key = key;
        this.value = value;
    }

    /**
     * The key of the property.
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * The value of the property.
     * @return the value
     */
    public String getValue() {
        return value;
    }
}
