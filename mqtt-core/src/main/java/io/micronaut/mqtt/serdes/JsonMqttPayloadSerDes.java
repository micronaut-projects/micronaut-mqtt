/*
 * Copyright 2017-2020 original authors
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
package io.micronaut.mqtt.serdes;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.core.reflect.ClassUtils;
import io.micronaut.core.serialize.exceptions.SerializationException;
import io.micronaut.core.type.Argument;
import io.micronaut.jackson.databind.JacksonDatabindMapper;
import io.micronaut.json.JsonMapper;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.IOException;

/**
 * Serializes and deserializes objects as JSON using Jackson.
 *
 * @author James Kleeh
 * @since 1.1.0
 */
@Singleton
public class JsonMqttPayloadSerDes implements MqttPayloadSerDes<Object> {

    /**
     * The order of this serDes.
     */
    public static final Integer ORDER = 200;

    private final JsonMapper jsonMapper;

    /**
     * Legacy jackson constructor.
     *
     * @param objectMapper The jackson object mapper
     * @deprecated Use {@link #JsonMqttPayloadSerDes(JsonMapper)} instead
     */
    @Deprecated
    public JsonMqttPayloadSerDes(ObjectMapper objectMapper) {
        this(new JacksonDatabindMapper(objectMapper));
    }

    /**
     * Default constructor.
     *
     * @param jsonMapper The json mapper
     * @since 2.1.0
     */
    @Inject
    public JsonMqttPayloadSerDes(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @Override
    public Object deserialize(byte[] payload, Argument<Object> type) {
        if (payload == null || payload.length == 0) {
            return null;
        }
        try {
            return jsonMapper.readValue(payload, type);
        } catch (IOException e) {
            throw new SerializationException("Error decoding JSON stream for type [" + type.getName() + "]: " + e.getMessage());
        }
    }

    @Override
    public byte[] serialize(Object data) {
        if (data == null) {
            return null;
        }
        try {
            return jsonMapper.writeValueAsBytes(data);
        } catch (IOException e) {
            throw new SerializationException("Error encoding object [" + data + "] to JSON: " + e.getMessage());
        }
    }

    @Override
    public int getOrder() {
        return ORDER;
    }

    @Override
    public boolean supports(Argument<Object> argument) {
        return !ClassUtils.isJavaBasicType(argument.getType());
    }
}
