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

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.io.Readable;

/**
 * MQTT client SSL configuration.
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@ConfigurationProperties("mqtt.client.ssl")
public class MqttCertificateConfiguration {

    private Readable certificateAuthority;
    private Readable certificate;
    private Readable privateKey;
    private char[] password;

    /**
     * @return The certificate authority
     */
    public Readable getCertificateAuthority() {
        return certificateAuthority;
    }

    /**
     * @param certificateAuthority The certificate authority location
     */
    public void setCertificateAuthority(Readable certificateAuthority) {
        this.certificateAuthority = certificateAuthority;
    }

    /**
     * @return The certificate
     */
    public Readable getCertificate() {
        return certificate;
    }

    /**
     * @param certificate The client certificate location
     */
    public void setCertificate(Readable certificate) {
        this.certificate = certificate;
    }

    /**
     * @return The client key
     */
    public Readable getPrivateKey() {
        return privateKey;
    }

    /**
     * @param privateKey The client private key location
     */
    public void setPrivateKey(Readable privateKey) {
        this.privateKey = privateKey;
    }

    /**
     * @return The key password
     */
    public char[] getPassword() {
        return password;
    }

    /**
     * @param password The key password
     */
    public void setPassword(char[] password) {
        this.password = password;
    }
}
