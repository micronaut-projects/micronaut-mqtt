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
package io.micronaut.mqtt.hivemq.client;

import io.micronaut.mqtt.hivemq.ssl.CertificateReader;
import io.micronaut.mqtt.hivemq.ssl.KeyManagerFactoryCreationException;
import io.micronaut.mqtt.hivemq.ssl.PrivateKeyReader;
import io.micronaut.mqtt.hivemq.ssl.TrustManagerFactoryCreationException;
import io.micronaut.mqtt.ssl.MqttCertificateConfiguration;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

/**
 * Common interface for MQTT client factories.
 *
 * @author Sven Kobow
 * @since 3.0.3
 * @see io.micronaut.mqtt.hivemq.v3.client.Mqtt3ClientFactory
 * @see io.micronaut.mqtt.hivemq.v5.client.Mqtt5ClientFactory
 */
public interface MqttClientFactory {

    default KeyManagerFactory getKeyManagerFactory(final MqttCertificateConfiguration certConfiguration) throws KeyManagerFactoryCreationException {
        try {
            final KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            final Certificate certificate = CertificateReader.readCertificate(certConfiguration.getCertificate());

            final PrivateKey key = PrivateKeyReader.getPrivateKey(certConfiguration.getPrivateKey(), certConfiguration.getPassword());

            keyStore.load(null, null);
            keyStore.setCertificateEntry("certificate", certificate);
            keyStore.setKeyEntry("private-key", key, certConfiguration.getPassword(), new Certificate[]{certificate});

            kmf.init(keyStore, certConfiguration.getPassword());

            return kmf;
        } catch (NoSuchAlgorithmException | KeyStoreException | IOException | CertificateException |
                 UnrecoverableKeyException e) {
            throw new KeyManagerFactoryCreationException(e.getMessage(), e);
        }
    }

    default TrustManagerFactory getTrustManagerFactory(final MqttCertificateConfiguration certConfiguration) throws TrustManagerFactoryCreationException {
        try {
            final TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            final KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());

            final Certificate certificate = CertificateReader.readCertificate(certConfiguration.getCertificateAuthority());

            keyStore.load(null);
            keyStore.setCertificateEntry("ca-certificate", certificate);

            tmf.init(keyStore);

            return tmf;
        } catch (NoSuchAlgorithmException | KeyStoreException | IOException | CertificateException e) {
            throw new TrustManagerFactoryCreationException(e.getMessage(), e);
        }
    }
}
