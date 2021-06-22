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
package io.micronaut.mqtt.ssl;

import io.micronaut.context.BeanProvider;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;
import io.micronaut.mqtt.config.MqttSSLConfiguration;
import jakarta.inject.Singleton;

import javax.net.SocketFactory;

@Singleton
class MqttConfigurationEventListener implements BeanCreatedEventListener<MqttSSLConfiguration> {

    private final BeanProvider<MqttCertificateConfiguration> certificateConfiguration;

    MqttConfigurationEventListener(BeanProvider<MqttCertificateConfiguration> certificateConfiguration) {
        this.certificateConfiguration = certificateConfiguration;
    }

    @Override
    public MqttSSLConfiguration onCreated(BeanCreatedEvent<MqttSSLConfiguration> event) {
        MqttCertificateConfiguration config = certificateConfiguration.get();
        SocketFactory socketFactory = MqttSslUtil.getSocketFactory(
                config.getCertificateAuthority(),
                config.getCertificate(),
                config.getPrivateKey(),
                config.getPassword());
        MqttSSLConfiguration configuration = event.getBean();
        configuration.setSocketFactory(socketFactory);
        return configuration;
    }
}
