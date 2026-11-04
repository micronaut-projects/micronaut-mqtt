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
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.DefaultBeanContext;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import io.micronaut.context.reload.ClassChange;
import io.micronaut.context.reload.ClassChangeEvent;
import io.micronaut.context.reload.ReloadStrategy;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.type.Argument;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.mqtt.annotation.MqttSubscriber;
import io.micronaut.mqtt.annotation.Topic;
import io.micronaut.mqtt.annotation.v5.MqttPublisher;
import io.micronaut.mqtt.bind.MqttBinderRegistry;
import io.micronaut.mqtt.bind.MqttBindingContext;
import io.micronaut.mqtt.bind.TypedMqttBinder;
import io.micronaut.mqtt.hivemq.intercept.MqttSubscriberAdvice;
import io.micronaut.mqtt.hivemq.v5.intercept.Mqtt5IntroductionAdvice;
import io.micronaut.mqtt.serdes.MqttPayloadSerDes;
import io.micronaut.mqtt.serdes.MqttPayloadSerDesRegistry;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The development reloader against a broker, with the HiveMQ client: what each change recreates, and that the
 * subscriptions deliver again, exactly once, on top of the new beans. Outside development mode the same beans and the
 * same events change nothing.
 */
class MqttReloaderTest {

    static final String TOPIC = "dev/reload";
    private static final String SPEC = "MqttReloaderTest";
    private static final String RELOADER = "io.micronaut.mqtt.intercept.DevelopmentMqttReloader";

    private ApplicationContext context;
    private Mqtt5BlockingClient publisher;

    @BeforeEach
    void connectPublisher() {
        // messages are published by a client of the test: the publisher advice of the application is recreated by
        // some of the changes, and a second call of a publisher method loses its payload (see send)
        publisher = MqttClient.builder()
            .useMqttVersion5()
            .identifier("publisher-" + UUID.randomUUID())
            .serverHost(HiveMQBroker.host())
            .serverPort(HiveMQBroker.port())
            .buildBlocking();
        publisher.connect();
    }

    @AfterEach
    void stop() {
        if (context != null) {
            context.close();
        }
        publisher.disconnect();
    }

    /**
     * Publishes through a client of the test. The application's {@link ReloadPublisher} is not used for that: with
     * the HiveMQ client, the second call of a publisher method publishes an empty payload, as the publisher state
     * caches the argument binders by the identity of the arguments of the first invocation.
     */
    private void send(String value) {
        publisher.publishWith().topic(TOPIC).qos(MqttQos.AT_LEAST_ONCE).payload(value.getBytes(StandardCharsets.UTF_8)).send();
    }

    @Test
    void anInPlaceChangeOfASubscriberClassResubscribesOnANewSubscriberAndARestartOrAnUnrelatedChangeDoesNot() throws Exception {
        devContext(true);
        MqttSubscriberAdvice advice = context.getBean(MqttSubscriberAdvice.class);
        ReloadSubscriber subscriber = context.getBean(ReloadSubscriber.class);
        assertTrue(context.containsBean(reloader()), "the reloader exists in development mode");
        sendAndAwait(subscriber, "one");

        // the application restarts: the new context subscribes again
        context.publishEvent(classChange(Set.of(ReloadSubscriber.class.getClassLoader()), List.of(), ReloadStrategy.RESTART));
        assertSame(advice, context.getBean(MqttSubscriberAdvice.class));

        // a class that is not a subscriber is redefined in place
        context.publishEvent(classChange(Set.of(), List.of(new ClassChange(MqttReloaderTest.class.getName(), ClassChange.Kind.MODIFIED)), ReloadStrategy.RELOAD));
        assertSame(advice, context.getBean(MqttSubscriberAdvice.class));
        assertSame(subscriber, context.getBean(ReloadSubscriber.class));

        // the subscriber class is redefined in place
        context.publishEvent(classChange(Set.of(), List.of(new ClassChange(ReloadSubscriber.class.getName(), ClassChange.Kind.MODIFIED)), ReloadStrategy.RELOAD));
        ReloadSubscriber recreated = context.getBean(ReloadSubscriber.class);
        assertNotSame(advice, context.getBean(MqttSubscriberAdvice.class));
        assertNotSame(subscriber, recreated);

        assertDeliveredOnce(recreated, 4);
        assertEquals(List.of("one"), subscriber.received, "the subscription of the previous subscriber was removed");
    }

    @Test
    void aSerDesChangeOrARetiredClassloaderRecreatesTheRegistriesThePublisherAdviceAndThePublishersAndResubscribes() throws Exception {
        devContext(true);
        ReloadPublisher publisher = context.getBean(ReloadPublisher.class);
        MqttPayloadSerDesRegistry serDes = context.getBean(MqttPayloadSerDesRegistry.class);
        MqttBinderRegistry binders = context.getBean(MqttBinderRegistry.class);
        Mqtt5IntroductionAdvice publishers = context.getBean(Mqtt5IntroductionAdvice.class);
        MqttSubscriberAdvice advice = context.getBean(MqttSubscriberAdvice.class);
        ReloadSubscriber subscriber = context.getBean(ReloadSubscriber.class);

        context.publishEvent(classChange(Set.of(), List.of(new ClassChange(LocaleSerDes.class.getName(), ClassChange.Kind.MODIFIED)), ReloadStrategy.RELOAD));
        assertNotSame(serDes, context.getBean(MqttPayloadSerDesRegistry.class));
        assertNotSame(binders, context.getBean(MqttBinderRegistry.class));
        assertNotSame(publishers, context.getBean(Mqtt5IntroductionAdvice.class));
        assertNotSame(publisher, context.getBean(ReloadPublisher.class));
        assertNotSame(advice, context.getBean(MqttSubscriberAdvice.class));
        assertSame(subscriber, context.getBean(ReloadSubscriber.class), "the subscriber bean is unchanged");
        assertDeliveredOnce(subscriber, 2);

        serDes = context.getBean(MqttPayloadSerDesRegistry.class);
        advice = context.getBean(MqttSubscriberAdvice.class);
        context.publishEvent(classChange(Set.of(ReloadSubscriber.class.getClassLoader()), List.of(), ReloadStrategy.RELOAD));
        assertNotSame(serDes, context.getBean(MqttPayloadSerDesRegistry.class));
        assertNotSame(advice, context.getBean(MqttSubscriberAdvice.class));
        assertDeliveredOnce(subscriber, 2);
    }

    @Test
    void anInPlaceChangeOfAFactoryThatProducesASerDesRecreatesTheRegistries() throws Exception {
        devContext(true);
        MqttPayloadSerDesRegistry serDes = context.getBean(MqttPayloadSerDesRegistry.class);
        context.publishEvent(classChange(Set.of(), List.of(new ClassChange(SerDesFactory.class.getName(), ClassChange.Kind.MODIFIED)), ReloadStrategy.RELOAD));
        assertNotSame(serDes, context.getBean(MqttPayloadSerDesRegistry.class));
        assertDeliveredOnce(context.getBean(ReloadSubscriber.class), 2);
    }

    @Test
    void aSubscriberDefinitionRegisteredWhileRunningResubscribes() throws Exception {
        devContext(true);
        MqttSubscriberAdvice advice = context.getBean(MqttSubscriberAdvice.class);
        BeanDefinition<ReloadSubscriber> definition = context.getBeanDefinition(ReloadSubscriber.class);

        // the launcher swaps the definition of the subscriber for another of the same class
        ((DefaultBeanContext) context).notifyDefinitionChange(List.of(definition), List.of(definition));

        assertNotSame(advice, context.getBean(MqttSubscriberAdvice.class));
        assertDeliveredOnce(context.getBean(ReloadSubscriber.class), 4);
    }

    @Test
    void aDefinitionOfABeanThatIsBothASerDesAndABinderResubscribesOnce() throws Exception {
        devContext(true);
        BeanDefinition<LocaleSerDes> definition = context.getBeanDefinition(LocaleSerDes.class);
        int created = AdviceCreations.COUNT.get();

        ((DefaultBeanContext) context).notifyDefinitionChange(List.of(definition), List.of(definition));

        assertEquals(created + 1, AdviceCreations.COUNT.get(), "the subscriber advice was created again exactly once");
        assertDeliveredOnce(context.getBean(ReloadSubscriber.class), 2);
    }

    @Test
    void aContextThatDoesNotTrackBeanDependenciesKeepsTheSubscriptions() throws Exception {
        devContext(false);
        MqttSubscriberAdvice advice = context.getBean(MqttSubscriberAdvice.class);
        ReloadSubscriber subscriber = context.getBean(ReloadSubscriber.class);
        assertTrue(context.containsBean(reloader()));

        context.publishEvent(classChange(Set.of(), List.of(new ClassChange(ReloadSubscriber.class.getName(), ClassChange.Kind.MODIFIED)), ReloadStrategy.RELOAD));

        assertSame(advice, context.getBean(MqttSubscriberAdvice.class));
        assertSame(subscriber, context.getBean(ReloadSubscriber.class));
        assertDeliveredOnce(subscriber, 2);
    }

    @Test
    void outsideDevelopmentModeThereIsNoReloaderAndTheSameBeansKeepTheirSubscriptionsThroughClassChanges() throws Exception {
        context = ApplicationContext.builder()
            .properties(properties())
            .start();
        assertFalse(context.containsBean(reloader()));
        MqttSubscriberAdvice advice = context.getBean(MqttSubscriberAdvice.class);
        MqttPayloadSerDesRegistry serDes = context.getBean(MqttPayloadSerDesRegistry.class);
        ReloadSubscriber subscriber = context.getBean(ReloadSubscriber.class);
        waitForSubscription(subscriber);

        context.publishEvent(classChange(Set.of(ReloadSubscriber.class.getClassLoader()),
            List.of(new ClassChange(ReloadSubscriber.class.getName(), ClassChange.Kind.MODIFIED)), ReloadStrategy.RELOAD));

        assertSame(advice, context.getBean(MqttSubscriberAdvice.class));
        assertSame(serDes, context.getBean(MqttPayloadSerDesRegistry.class));
        assertSame(subscriber, context.getBean(ReloadSubscriber.class));
        assertDeliveredOnce(subscriber, 2);
    }

    private void devContext(boolean track) throws InterruptedException {
        Map<String, Object> properties = properties();
        properties.put("micronaut.dev.enabled", "true");
        context = ApplicationContext.builder()
            .properties(properties)
            .beanDependencyTrackingEnabled(track)
            .start();
        waitForSubscription(context.getBean(ReloadSubscriber.class));
    }

    private static Map<String, Object> properties() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("mqtt.client.server-uri", HiveMQBroker.serverUri());
        properties.put("mqtt.client.client-id", "reloader-" + UUID.randomUUID());
        properties.put("spec.name", SPEC);
        return properties;
    }

    /**
     * The subscription is acknowledged asynchronously: publishes until the subscriber receives one.
     */
    private void waitForSubscription(ReloadSubscriber subscriber) throws InterruptedException {
        long deadline = System.nanoTime() + Duration.ofSeconds(30).toNanos();
        while (subscriber.received.stream().noneMatch(value -> value.startsWith("ready"))) {
            if (System.nanoTime() > deadline) {
                throw new AssertionError("Timed out waiting for the subscription");
            }
            send("ready");
            Thread.sleep(200);
        }
        subscriber.received.clear();
    }

    private void sendAndAwait(ReloadSubscriber subscriber, String value) throws InterruptedException {
        send(value);
        awaitTrue(subscriber + " receives " + value, () -> subscriber.received.contains(value));
    }

    /**
     * Publishes the given number of messages once the new subscription is in place, and checks that each reaches the
     * subscriber once: a subscription of a previous subscriber advice left on the client would deliver it twice.
     */
    private void assertDeliveredOnce(ReloadSubscriber subscriber, int count) throws InterruptedException {
        waitForSubscription(subscriber);
        List<String> sent = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String value = "message-" + UUID.randomUUID();
            sent.add(value);
            send(value);
        }
        awaitTrue(subscriber + " receives " + sent, () -> subscriber.received.containsAll(sent));
        Thread.sleep(500);
        for (String value : sent) {
            assertEquals(1, subscriber.received.stream().filter(value::equals).count(), value + " was delivered once: " + subscriber.received);
        }
    }

    private ClassChangeEvent classChange(Set<ClassLoader> retired, List<ClassChange> changes, ReloadStrategy strategy) {
        return new ClassChangeEvent(this, retired, MqttReloaderTest.class.getClassLoader(), changes, strategy);
    }

    private static Class<?> reloader() throws ClassNotFoundException {
        return Class.forName(RELOADER);
    }

    static void awaitTrue(String what, BooleanSupplier condition) throws InterruptedException {
        long deadline = System.nanoTime() + Duration.ofSeconds(30).toNanos();
        while (!condition.getAsBoolean()) {
            if (System.nanoTime() > deadline) {
                throw new AssertionError("Timed out waiting until " + what);
            }
            Thread.sleep(50);
        }
    }

    @MqttSubscriber
    @Requires(property = "spec.name", value = SPEC)
    static class ReloadSubscriber {
        final List<String> received = new CopyOnWriteArrayList<>();

        @Topic(TOPIC)
        void receive(String value) {
            received.add(value);
        }
    }

    @MqttPublisher
    @Requires(property = "spec.name", value = SPEC)
    interface ReloadPublisher {
        @Topic(TOPIC)
        void send(String value);
    }

    @Singleton
    @Requires(property = "spec.name", value = SPEC)
    static class LocaleSerDes implements MqttPayloadSerDes<Locale>, TypedMqttBinder<MqttBindingContext<?>, Locale> {
        @Override
        public Locale deserialize(byte[] payload, Argument<Locale> argument) {
            return payload == null ? null : Locale.forLanguageTag(new String(payload, StandardCharsets.UTF_8));
        }

        @Override
        public byte[] serialize(Locale data) {
            return data == null ? null : data.toLanguageTag().getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public boolean supports(Argument<Locale> type) {
            return type.getType() == Locale.class;
        }

        @Override
        public Argument<Locale> getArgumentType() {
            return Argument.of(Locale.class);
        }

        @Override
        public void bindTo(MqttBindingContext<?> context, Locale value, Argument<Locale> argument) {
        }

        @Override
        public Optional<Locale> bindFrom(MqttBindingContext<?> context, ArgumentConversionContext<Locale> conversionContext) {
            return Optional.empty();
        }
    }

    @Factory
    @Requires(property = "spec.name", value = SPEC)
    static class SerDesFactory {
        @Singleton
        UuidSerDes uuidSerDes() {
            return new UuidSerDes();
        }
    }

    static class UuidSerDes implements MqttPayloadSerDes<UUID> {
        @Override
        public UUID deserialize(byte[] payload, Argument<UUID> argument) {
            return payload == null ? null : UUID.fromString(new String(payload, StandardCharsets.UTF_8));
        }

        @Override
        public byte[] serialize(UUID data) {
            return data == null ? null : data.toString().getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public boolean supports(Argument<UUID> type) {
            return type.getType() == UUID.class;
        }
    }

    @Singleton
    @Requires(property = "spec.name", value = SPEC)
    static class AdviceCreations implements BeanCreatedEventListener<MqttSubscriberAdvice> {
        static final AtomicInteger COUNT = new AtomicInteger();

        @Override
        public MqttSubscriberAdvice onCreated(BeanCreatedEvent<MqttSubscriberAdvice> event) {
            COUNT.incrementAndGet();
            return event.getBean();
        }
    }
}
