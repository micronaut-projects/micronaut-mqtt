package io.micronaut.mqtt.v3.client;

import edu.umd.cs.findbugs.annotations.Nullable;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.mqtt.v3.config.MqttClientConfigurationProperties;
import io.micronaut.scheduling.TaskExecutors;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.internal.HighResolutionTimer;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

@Factory
public class MqttClientFactory {

    @Singleton
    @Bean(preDestroy = "disconnect")
    MqttAsyncClient mqttClient(MqttClientConfigurationProperties configuration,
                               @Nullable MqttClientPersistence clientPersistence,
                               @Nullable HighResolutionTimer highResolutionTimer,
                               @Named(TaskExecutors.MESSAGE_CONSUMER) ExecutorService executorService) throws MqttException {
        ScheduledExecutorService consumerExecutor = (ScheduledExecutorService) executorService;
        MqttAsyncClient client = new MqttAsyncClient(configuration.getServerUri(), configuration.getClientId(), clientPersistence, new ScheduledExecutorPingSender(consumerExecutor), consumerExecutor, highResolutionTimer);
        client.connect(configuration.getConnectOptions())
                .waitForCompletion(configuration.getConnectionTimeout().toMillis());
        return client;
    }
}
