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
package io.micronaut.mqtt.hivemq.ssl;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.io.Readable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.Security;

/**
 * Reader for reading PKCS #1 and PKCS #8 private keys from PEM files.
 *
 * @author Sven Kobow
 * @since 3.0.0
 */
@Internal
public final class PrivateKeyReader {

    private static final Logger LOG = LoggerFactory.getLogger(PrivateKeyReader.class);

    private PrivateKeyReader() {
    }

    public static PrivateKey getPrivateKey(final Readable keyFile, final char[] password) {
        try {
            PrivateKey key;
            Security.addProvider(new BouncyCastleProvider());

            try (PEMParser parser = new PEMParser(keyFile.asReader())) {
                final JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
                final Object keyObject = parser.readObject();
                KeyPair keyPair;
                if (keyObject instanceof PEMEncryptedKeyPair encryptedKeyPair) {
                    PEMDecryptorProvider provider = new JcePEMDecryptorProviderBuilder().build(password);
                    keyPair = converter.getKeyPair(encryptedKeyPair.decryptKeyPair(provider));
                } else {
                    keyPair = converter.getKeyPair((PEMKeyPair) keyObject);
                }

                key = keyPair.getPrivate();
            }

            return key;
        } catch (Exception e) {
            LOG.error("Error reading private key", e);
        }

        return null;
    }
}
