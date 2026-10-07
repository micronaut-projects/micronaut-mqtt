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
package io.micronaut.mqtt.dev.pahov5;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.reload.ClassChange;
import io.micronaut.context.reload.ClassChangeEvent;
import io.micronaut.context.reload.ReloadStrategy;
import io.micronaut.dev.tck.ReloadHarness;
import io.micronaut.dev.tck.ReloadTck;
import org.eclipse.paho.mqttv5.client.MqttAsyncClient;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.persist.MemoryPersistence;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Runs an application with an MQTT subscriber, on the Paho v5 client, through the development runtime against a
 * broker, and edits the subscriber. A change applied in place subscribes the new subscriber once. A restart keeps the
 * client, its connection and the consumer executor it runs on: the retired generation unsubscribes as its context
 * stops, the new one subscribes on the same connection, and nothing of the retired generation stays reachable. A
 * change under {@code mqtt.client} releases the client.
 */
class PahoV5ReloadTest {

    private static final String TOPIC = "dev/reload/paho-v5";

    private static final String SUBSCRIBER = """
        package example;

        import io.micronaut.mqtt.annotation.MqttSubscriber;
        import io.micronaut.mqtt.annotation.Topic;

        import java.util.List;
        import java.util.concurrent.CopyOnWriteArrayList;

        @MqttSubscriber
        public class Subscriber {
            private final List<String> received = new CopyOnWriteArrayList<>();

            @Topic("%s")
            public void receive(String value) {
                received.add("%s " + value);
            }

            public List<String> received() {
                return received;
            }
        }
        """;

    @TempDir
    Path project;

    @Test
    void anInPlaceChangeResubscribesOnceAndARestartKeepsTheClientAndLeavesTheRetiredGenerationCollectable() throws Exception {
        MqttClient publisher = publisher();
        try (ReloadHarness harness = ReloadHarness.inDirectory(project)) {
            configure(harness);
            harness.source("example.Subscriber", SUBSCRIBER.formatted(TOPIC, "first"));
            harness.start();
            assertTrue(harness.context().containsBean(type(harness.context(), "io.micronaut.mqtt.intercept.DevelopmentMqttReloader")));

            awaitSubscribed(harness, publisher, "first");
            Object subscriber = subscriber(harness.context());
            changedInPlace(harness, "example.Subscriber");
            assertNotSame(subscriber, subscriber(harness.context()));
            awaitSubscribed(harness, publisher, "first");
            assertDeliveredOnce(harness, publisher, "first");
            subscriber = null;

            MqttAsyncClient client = harness.context().getBean(MqttAsyncClient.class);
            harness.source("example.Subscriber", SUBSCRIBER.formatted(TOPIC, "second"));
            long reloadStart = System.nanoTime();
            harness.reload();
            assertEquals(2, harness.generation());

            // the client, still connected, is kept with the consumer executor it runs on
            ReloadTck.assertRetained(harness, client);
            assertSame(client, harness.context().getBean(MqttAsyncClient.class));
            assertTrue(client.isConnected());
            client = null;

            // the retired context unsubscribed its topics as it stopped: every message reaches the second generation once
            awaitSubscribed(harness, publisher, "second");
            System.out.println("The second generation received a message " + Duration.ofNanos(System.nanoTime() - reloadStart).toMillis() + " ms after the reload started");
            assertDeliveredOnce(harness, publisher, "second");
            assertFalse(received(harness.context()).stream().anyMatch(value -> value.startsWith("first")));

            // neither the subscriptions of the first generation, the retained client nor its executor keep it reachable
            ReloadTck.assertRetiredGenerationsCollected(harness);
        } finally {
            publisher.disconnect();
            publisher.close();
        }
    }

    @Test
    void aChangeUnderTheClientPrefixReleasesTheClient() throws Exception {
        MqttClient publisher = publisher();
        try (ReloadHarness harness = ReloadHarness.inDirectory(project)) {
            String clientId = configure(harness);
            harness.source("example.Subscriber", SUBSCRIBER.formatted(TOPIC, "first"));
            harness.start();
            MqttAsyncClient first = harness.context().getBean(MqttAsyncClient.class);

            // the application properties change under mqtt.client, together with a class, so the application restarts
            harness.resource("application.properties", """
                mqtt.client.server-uri=%s
                mqtt.client.client-id=%s
                mqtt.client.keep-alive-interval=30
                """.formatted(HiveMQBroker.serverUri(), clientId));
            harness.source("example.Subscriber", SUBSCRIBER.formatted(TOPIC, "second"));
            harness.reload();
            assertEquals(2, harness.generation());

            MqttAsyncClient second = harness.context().getBean(MqttAsyncClient.class);
            assertNotSame(first, second, "a change under mqtt.client releases the client");
            assertFalse(first.isConnected(), "the released client is disconnected");
            awaitSubscribed(harness, publisher, "second");
            assertDeliveredOnce(harness, publisher, "second");
            first = null;
            second = null;
            ReloadTck.assertRetiredGenerationsCollected(harness);
        } finally {
            publisher.disconnect();
            publisher.close();
        }
    }

    private static String configure(ReloadHarness harness) {
        String clientId = "paho-" + UUID.randomUUID();
        harness.property("mqtt.client.server-uri", HiveMQBroker.serverUri());
        harness.property("mqtt.client.client-id", clientId);
        return clientId;
    }

    private static MqttClient publisher() throws MqttException {
        MqttClient publisher = new MqttClient(HiveMQBroker.serverUri(), "publisher-" + UUID.randomUUID(), new MemoryPersistence());
        publisher.connect();
        return publisher;
    }

    private static void publish(MqttClient publisher, String value) throws MqttException {
        publisher.publish(TOPIC, value.getBytes(StandardCharsets.UTF_8), 1, false);
    }

    private static void awaitSubscribed(ReloadHarness harness, MqttClient publisher, String generation) throws Exception {
        String ready = "ready-" + UUID.randomUUID();
        long deadline = System.nanoTime() + Duration.ofSeconds(60).toNanos();
        while (!received(harness.context()).contains(generation + " " + ready)) {
            if (System.nanoTime() > deadline) {
                throw new AssertionError("Timed out waiting for the subscription of the " + generation + " subscriber");
            }
            publish(publisher, ready);
            Thread.sleep(200);
        }
    }

    private static void assertDeliveredOnce(ReloadHarness harness, MqttClient publisher, String generation) throws Exception {
        List<String> expected = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            String value = "message-" + UUID.randomUUID();
            expected.add(generation + " " + value);
            publish(publisher, value);
        }
        awaitTrue("the " + generation + " subscriber receives " + expected, () -> received(harness.context()).containsAll(expected));
        Thread.sleep(500);
        List<String> received = received(harness.context());
        for (String value : expected) {
            assertEquals(1, received.stream().filter(value::equals).count(), value + " was delivered once: " + received);
        }
    }

    private static void changedInPlace(ReloadHarness harness, String className) {
        ApplicationContext context = harness.context();
        context.publishEvent(new ClassChangeEvent(PahoV5ReloadTest.class, harness.generation(), Set.of(), context.getClassLoader(),
            List.of(new ClassChange(className, ClassChange.Kind.MODIFIED)), ReloadStrategy.RELOAD));
    }

    private static void awaitTrue(String what, BooleanSupplier condition) throws InterruptedException {
        long deadline = System.nanoTime() + Duration.ofSeconds(60).toNanos();
        while (!condition.getAsBoolean()) {
            if (System.nanoTime() > deadline) {
                throw new AssertionError("Timed out waiting until " + what);
            }
            Thread.sleep(100);
        }
    }

    private static Object subscriber(ApplicationContext context) {
        return context.getBean(type(context, "example.Subscriber"));
    }

    @SuppressWarnings("unchecked")
    private static List<String> received(ApplicationContext context) {
        try {
            return (List<String>) type(context, "example.Subscriber").getMethod("received").invoke(subscriber(context));
        } catch (ReflectiveOperationException e) {
            throw new AssertionError("Cannot read what the subscriber received", e);
        }
    }

    private static Class<?> type(ApplicationContext context, String className) {
        try {
            return Class.forName(className, true, context.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new AssertionError(className + " is not in the application", e);
        }
    }
}
