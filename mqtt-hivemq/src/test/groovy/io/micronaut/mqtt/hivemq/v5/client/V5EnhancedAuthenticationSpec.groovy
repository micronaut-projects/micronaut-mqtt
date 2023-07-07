package io.micronaut.mqtt.hivemq.v5.client

import com.hivemq.client.mqtt.datatypes.MqttUtf8String
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientConfig
import com.hivemq.client.mqtt.mqtt5.auth.Mqtt5EnhancedAuthMechanism
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5ConnAckException
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5Auth
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5AuthBuilder
import com.hivemq.client.mqtt.mqtt5.message.auth.Mqtt5EnhancedAuthBuilder
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5Disconnect
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.context.exceptions.BeanInstantiationException
import io.micronaut.mqtt.test.AbstractMQTTTest
import io.micronaut.mqtt.test.MQTT5Test
import jakarta.inject.Singleton
import org.jetbrains.annotations.NotNull

import java.util.concurrent.CompletableFuture

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

class V5EnhancedAuthenticationSpec extends AbstractMQTTTest implements MQTT5Test {

    /**
     * This test tests whether the optional Mqtt5EnhancedAuthenticationMechanism was passed and set correctly.
     * It is expected to throw an exception as enhanced authentication is not supported by the test broker.
     */
    void "test enhanced authentication mechanism"() {
        ApplicationContext ctx = startContext("enhancedauthmechanismtest": true)

        when:
        def client = ctx.getBean(Mqtt5AsyncClient)

        then:
        final BeanInstantiationException exception = thrown() // bean instantiation will fail due to unsupported auth method -> enhanced auth was correctly applied
    }

    @Singleton
    @Requires(property = "enhancedauthmechanismtest")
    static class MyEnhancedAuthenticationMechanism implements Mqtt5EnhancedAuthMechanism {

        @Override
        MqttUtf8String getMethod() {
            return MqttUtf8String.of("SCRAM-SHA-1") // will fail as it is not supported
        }

        @Override
        int getTimeout() {
            return 0
        }

        @Override
        CompletableFuture<Void> onAuth(@NotNull Mqtt5ClientConfig clientConfig, @NotNull Mqtt5Connect connect, @NotNull Mqtt5EnhancedAuthBuilder authBuilder) {
            return CompletableFuture.completedFuture()
        }

        @Override
        CompletableFuture<Void> onReAuth(@NotNull Mqtt5ClientConfig clientConfig, @NotNull Mqtt5AuthBuilder authBuilder) {
            return CompletableFuture.completedFuture()
        }

        @Override
        CompletableFuture<Boolean> onContinue(@NotNull Mqtt5ClientConfig clientConfig, @NotNull Mqtt5Auth auth, @NotNull Mqtt5AuthBuilder authBuilder) {
            return CompletableFuture.completedFuture(true)
        }

        @Override
        CompletableFuture<Boolean> onAuthSuccess(@NotNull Mqtt5ClientConfig clientConfig, @NotNull Mqtt5ConnAck connAck) {
            return null
        }

        @Override
        CompletableFuture<Boolean> onReAuthSuccess(@NotNull Mqtt5ClientConfig clientConfig, @NotNull Mqtt5Auth auth) {
            return CompletableFuture.completedFuture(true)
        }

        @Override
        void onAuthRejected(@NotNull Mqtt5ClientConfig clientConfig, @NotNull Mqtt5ConnAck connAck) {

        }

        @Override
        void onReAuthRejected(@NotNull Mqtt5ClientConfig clientConfig, @NotNull Mqtt5Disconnect disconnect) {

        }

        @Override
        void onAuthError(@NotNull Mqtt5ClientConfig clientConfig, @NotNull Throwable cause) {

        }

        @Override
        void onReAuthError(@NotNull Mqtt5ClientConfig clientConfig, @NotNull Throwable cause) {

        }
    }
}
