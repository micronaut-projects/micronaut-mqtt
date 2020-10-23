package io.micronaut.mqtt.executor;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.executor.ExecutorConfiguration;
import io.micronaut.scheduling.executor.ExecutorType;
import io.micronaut.scheduling.executor.UserExecutorConfiguration;

import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Configures a {@link java.util.concurrent.ScheduledExecutorService} for running {@link} instances.
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@Factory
public class MqttExecutorFactory {

    /**
     * @return The executor configurations
     */
    @Singleton
    @Requires(missingProperty = ExecutorConfiguration.PREFIX_CONSUMER)
    @Named(TaskExecutors.MESSAGE_CONSUMER)
    ExecutorConfiguration executor() {
        return UserExecutorConfiguration.of(ExecutorType.SCHEDULED);
    }

}
