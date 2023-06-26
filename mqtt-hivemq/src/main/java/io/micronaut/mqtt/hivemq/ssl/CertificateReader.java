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
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMParser;

import java.security.Security;
import java.security.cert.Certificate;

public final class CertificateReader {
    public static Certificate readCertificate(final Readable crtFile) {
        try {
            Security.addProvider(new BouncyCastleProvider());

            final JcaX509CertificateConverter certificateConverter = new JcaX509CertificateConverter().setProvider("BC");

            try (PEMParser parser = new PEMParser(crtFile.asReader())) {
                final X509CertificateHolder certHolder = (X509CertificateHolder) parser.readObject();

                return certificateConverter.getCertificate(certHolder);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
