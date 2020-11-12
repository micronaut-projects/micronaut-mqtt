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

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import io.micronaut.core.io.Readable;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

class MqttSslUtil {

    static SSLSocketFactory getSocketFactory(final Readable caCrtFile,
                                                    final Readable crtFile,
                                                    final Readable keyFile,
                                                    final char[] password) {
        try {

            //Add BouncyCastle as a Security Provider
            Security.addProvider(new BouncyCastleProvider());

            JcaX509CertificateConverter certificateConverter = new JcaX509CertificateConverter().setProvider("BC");

            //Load Certificate Authority (CA) certificate
            PEMParser reader = new PEMParser(caCrtFile.asReader());
            X509CertificateHolder caCertHolder = (X509CertificateHolder) reader.readObject();
            reader.close();

            X509Certificate caCert = certificateConverter.getCertificate(caCertHolder);

            //Load client certificate
            reader = new PEMParser(crtFile.asReader());
            X509CertificateHolder certHolder = (X509CertificateHolder) reader.readObject();
            reader.close();

            X509Certificate cert = certificateConverter.getCertificate(certHolder);

            //Load client private key
            reader = new PEMParser(keyFile.asReader());
            Object keyObject = reader.readObject();
            reader.close();

            PEMDecryptorProvider provider = new JcePEMDecryptorProviderBuilder().build(password);
            JcaPEMKeyConverter keyConverter = new JcaPEMKeyConverter().setProvider("BC");

            KeyPair key;

            if (keyObject instanceof PEMEncryptedKeyPair) {
                key = keyConverter.getKeyPair(((PEMEncryptedKeyPair) keyObject).decryptKeyPair(provider));
            } else {
                key = keyConverter.getKeyPair((PEMKeyPair) keyObject);
            }

            // CA certificate is used to authenticate server
            KeyStore caKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            caKeyStore.load(null, null);
            caKeyStore.setCertificateEntry("ca-certificate", caCert);

            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(caKeyStore);

            // Client key and certificates are sent to server so it can authenticate the client
            KeyStore clientKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            clientKeyStore.load(null, null);
            clientKeyStore.setCertificateEntry("certificate", cert);
            clientKeyStore.setKeyEntry("private-key", key.getPrivate(), password,
                    new Certificate[]{cert});

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
                    KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(clientKeyStore, password);

            // Create SSL socket factory
            SSLContext context = SSLContext.getInstance("TLSv1.2");
            context.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);

            // Return the newly created socket factory object
            return context.getSocketFactory();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
