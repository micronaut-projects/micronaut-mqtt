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
package io.micronaut.mqtt.dev;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.reload.ClassChange;
import io.micronaut.context.reload.ClassChangeEvent;
import io.micronaut.context.reload.ReloadStrategy;
import io.micronaut.dev.tck.ReloadHarness;
import io.micronaut.dev.tck.ReloadTck;
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
 * Runs an application with an MQTT subscriber, on the HiveMQ client, through the development runtime against a
 * broker, and edits the subscriber. A change applied in place subscribes the new subscriber; a restart unsubscribes the
 * retired generation as its context stops, keeps the client and its connection, on which the new generation subscribes
 * afresh, and nothing of the retired generation stays reachable. A change under {@code mqtt.client} releases the
 * client.
 */
class MqttReloadTest {

    private static final String TOPIC = "dev/reload/harness";

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
    void anInPlaceChangeResubscribesAndARestartKeepsTheClientAndLeavesTheRetiredGenerationCollectable() throws Exception {
        Mqtt5BlockingClient publisher = publisher();
        try (ReloadHarness harness = ReloadHarness.inDirectory(project)) {
            configure(harness);
            harness.source("example.Subscriber", SUBSCRIBER.formatted(TOPIC, "first"));
            harness.start();
            assertReloaderPresent(harness.context());

            awaitSubscribed(harness, publisher, "first");
            ReloadTck.assertFollowsReload(harness, MqttReloadTest::subscriber);

            // the subscriber class changed in place: the topics are subscribed again for a new subscriber bean
            Object subscriber = subscriber(harness.context());
            long inPlaceStart = System.nanoTime();
            changedInPlace(harness, "example.Subscriber");
            assertNotSame(subscriber, subscriber(harness.context()));
            awaitSubscribed(harness, publisher, "first");
            System.out.println("The new subscription received a message " + millisSince(inPlaceStart) + " ms after the change");
            assertDeliveredOnce(harness, publisher, "first");
            subscriber = null;

            Mqtt5AsyncClient client = harness.context().getBean(Mqtt5AsyncClient.class);

            harness.source("example.Subscriber", SUBSCRIBER.formatted(TOPIC, "second"));
            long reloadStart = System.nanoTime();
            harness.reload();
            assertEquals(2, harness.generation());
            assertReloaderPresent(harness.context());

            // the client, still connected, is kept
            ReloadTck.assertRetained(harness, client);
            assertSame(client, harness.context().getBean(Mqtt5AsyncClient.class));
            assertTrue(client.getState().isConnected());

            // the retired context unsubscribed its topics as it stopped: every message reaches the second generation once
            awaitSubscribed(harness, publisher, "second");
            System.out.println("The second generation received a message " + millisSince(reloadStart) + " ms after the reload started");
            assertDeliveredOnce(harness, publisher, "second");
            assertFalse(received(harness.context()).stream().anyMatch(value -> value.startsWith("first")));
            ReloadTck.assertFollowsReload(harness, MqttReloadTest::subscriber);
            client = null;

            // neither the subscriptions of the first generation, the retained client nor the development-only
            // reloader keep it reachable
            ReloadTck.assertRetiredGenerationsCollected(harness);
        } finally {
            publisher.disconnect();
        }
    }

    @Test
    void aChangeUnderTheClientPrefixReleasesTheClient() throws Exception {
        Mqtt5BlockingClient publisher = publisher();
        try (ReloadHarness harness = ReloadHarness.inDirectory(project)) {
            String clientId = configure(harness);
            harness.source("example.Subscriber", SUBSCRIBER.formatted(TOPIC, "first"));
            harness.start();
            Mqtt5AsyncClient first = harness.context().getBean(Mqtt5AsyncClient.class);

            // the application properties change under mqtt.client, together with a class, so the application restarts
            harness.resource("application.properties", """
                mqtt.client.server-uri=%s
                mqtt.client.client-id=%s
                mqtt.client.keep-alive-interval=30
                """.formatted(HiveMQBroker.serverUri(), clientId));
            harness.source("example.Subscriber", SUBSCRIBER.formatted(TOPIC, "second"));
            harness.reload();
            assertEquals(2, harness.generation());

            Mqtt5AsyncClient second = harness.context().getBean(Mqtt5AsyncClient.class);
            assertNotSame(first, second, "a change under mqtt.client releases the client");
            assertFalse(first.getState().isConnected(), "the released client is disconnected");
            awaitSubscribed(harness, publisher, "second");
            first = null;
            second = null;
            ReloadTck.assertRetiredGenerationsCollected(harness);
        } finally {
            publisher.disconnect();
        }
    }

    private static String configure(ReloadHarness harness) {
        String clientId = "harness-" + UUID.randomUUID();
        harness.property("mqtt.client.server-uri", HiveMQBroker.serverUri());
        harness.property("mqtt.client.client-id", clientId);
        return clientId;
    }

    private static Mqtt5BlockingClient publisher() {
        Mqtt5BlockingClient client = MqttClient.builder()
            .useMqttVersion5()
            .identifier("publisher-" + UUID.randomUUID())
            .serverHost(HiveMQBroker.host())
            .serverPort(HiveMQBroker.port())
            .buildBlocking();
        client.connect();
        return client;
    }

    private static void publish(Mqtt5BlockingClient publisher, String value) {
        publisher.publishWith().topic(TOPIC).payload(value.getBytes(StandardCharsets.UTF_8)).send();
    }

    /**
     * The subscription is acknowledged asynchronously: publishes until the subscriber of the current generation
     * receives one.
     */
    private static void awaitSubscribed(ReloadHarness harness, Mqtt5BlockingClient publisher, String generation) throws InterruptedException {
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

    /**
     * Each message reaches the current subscriber exactly once: a subscription left by a previous subscriber advice on
     * the client would deliver it twice, or to the previous subscriber.
     */
    private static void assertDeliveredOnce(ReloadHarness harness, Mqtt5BlockingClient publisher, String generation) throws InterruptedException {
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

    private static long millisSince(long start) {
        return Duration.ofNanos(System.nanoTime() - start).toMillis();
    }

    /**
     * Tells the running generation that a class was redefined in place, as the development runtime does after it
     * redefined the class. The context is not kept: a reference to it would keep the generation reachable.
     */
    private static void changedInPlace(ReloadHarness harness, String className) {
        ApplicationContext context = harness.context();
        context.publishEvent(new ClassChangeEvent(MqttReloadTest.class, harness.generation(), Set.of(), context.getClassLoader(),
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

    private static void assertReloaderPresent(ApplicationContext context) {
        // the bean that follows changes in place exists in development mode only
        assertTrue(context.containsBean(type(context, "io.micronaut.mqtt.intercept.DevelopmentMqttReloader")));
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
