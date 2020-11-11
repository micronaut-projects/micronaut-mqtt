package io.micronaut.mqtt.docs.publisher.acknowledge;

import io.micronaut.mqtt.AbstractRabbitMQTest;
import io.micronaut.context.ApplicationContext;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.Maybe;
import io.reactivex.MaybeObserver;
import io.reactivex.disposables.Disposable;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

public class PublisherAcknowledgeSpec extends AbstractRabbitMQTest {

    @Test
    void testPublisherAcknowledgement() {
        ApplicationContext applicationContext = startContext();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);


        ProductClient productClient = applicationContext.getBean(ProductClient.class);
        Completable completable = productClient.send("completable body".getBytes());
        Maybe<Void> maybe = productClient.sendMaybe("maybe body".getBytes());
        Mono<Void> mono = productClient.sendMono("mono body".getBytes());
        Publisher<Void> publisher = productClient.sendPublisher("publisher body".getBytes());
        CompletableFuture<Void> future = productClient.sendFuture("future body".getBytes());

        ProductListener listener = applicationContext.getBean(ProductListener.class);

        completable.subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) { }

            @Override
            public void onComplete() {
                // if the publish was acknowledged
                successCount.incrementAndGet();
            }

            @Override
            public void onError(Throwable e) {
                // if an error occurs
                errorCount.incrementAndGet();
            }
        });
        maybe.subscribe(new MaybeObserver<Void>() {
            @Override
            public void onSubscribe(Disposable d) { }

            @Override
            public void onSuccess(Void aVoid) {
                throw new UnsupportedOperationException("Should never be called");
            }

            @Override
            public void onError(Throwable e) {
                // if an error occurs
                errorCount.incrementAndGet();
            }

            @Override
            public void onComplete() {
                // if the publish was acknowledged
                successCount.incrementAndGet();
            }
        });
        Subscriber<Void> subscriber = new Subscriber<Void>() {
            @Override
            public void onSubscribe(Subscription subscription) { }

            @Override
            public void onNext(Void aVoid) {
                throw new UnsupportedOperationException("Should never be called");
            }

            @Override
            public void onError(Throwable throwable) {
                // if an error occurs
                errorCount.incrementAndGet();
            }

            @Override
            public void onComplete() {
                // if the publish was acknowledged
                successCount.incrementAndGet();
            }
        };
        mono.subscribe(subscriber);
        publisher.subscribe(subscriber);
        future.whenComplete((v, t) -> {
            if (t == null) {
                successCount.incrementAndGet();
            } else {
                errorCount.incrementAndGet();
            }
        });

        try {
            await().atMost(5, SECONDS).until(() ->
                    errorCount.get() == 0 &&
                    successCount.get() == 5 &&
                    listener.messageLengths.size() == 5
            );
        } finally {
            applicationContext.close();
        }
    }

}
