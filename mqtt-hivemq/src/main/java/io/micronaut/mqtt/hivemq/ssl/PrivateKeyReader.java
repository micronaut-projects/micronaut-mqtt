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

import io.micronaut.core.io.Readable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMDecryptorProvider;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.Security;

import static org.bouncycastle.cms.RecipientId.password;

public final class PrivateKeyReader {
    public static PrivateKey getPrivateKey(final Readable keyFile, final char[] password) {
        try {
            PrivateKey key;
            Security.addProvider(new BouncyCastleProvider());

            try (PEMParser parser = new PEMParser(keyFile.asReader())) {
                final JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");
                final Object keyObject = parser.readObject();
                KeyPair keyPair;
                if (keyObject instanceof PEMEncryptedKeyPair) {
                    PEMDecryptorProvider provider = new JcePEMDecryptorProviderBuilder().build(password);
                    keyPair = converter.getKeyPair(((PEMEncryptedKeyPair) keyObject).decryptKeyPair(provider));
                } else {
                    keyPair = converter.getKeyPair((PEMKeyPair) keyObject);
                }

                key = keyPair.getPrivate();
            }

            return key;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
