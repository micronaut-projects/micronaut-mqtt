package io.micronaut.mqtt.v5.client;

import edu.umd.cs.findbugs.annotations.Nullable;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.mqtt.v5.config.MqttClientConfigurationProperties;
import io.micronaut.scheduling.TaskExecutors;
import org.eclipse.paho.mqttv5.client.MqttAsyncClient;
import org.eclipse.paho.mqttv5.client.MqttClientPersistence;
import org.eclipse.paho.mqttv5.client.TimerPingSender;
import org.eclipse.paho.mqttv5.common.MqttException;

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
                               @Named(TaskExecutors.MESSAGE_CONSUMER) ExecutorService executorService) throws MqttException {
        ScheduledExecutorService consumerExecutor = (ScheduledExecutorService) executorService;
        MqttAsyncClient client = new MqttAsyncClient(configuration.getServerUri(), configuration.getClientId(), clientPersistence, new TimerPingSender(consumerExecutor), consumerExecutor);
        client.connect(configuration.getConnectOptions()).waitForCompletion();

        client.subscribe()
        return client;
    }
}
