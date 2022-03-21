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
package io.micronaut.azure.secretmanager.client;

import java.util.List;

/**
 * This interface is intended to abstract interactions with {@link com.azure.security.keyvault.secrets.SecretClient}.
 */
public interface SecretKeyvaultClient {

    /**
     * Fetches a secret from the Keyvault using name of the secret.
     *
     * @param secretName - name of the secret
     * @return String value of the secret or empty
     */
    VersionedSecret getSecret(String secretName);

    /**
     * Fetches all secrets from the Keyvault.
     *
     * @return List of all secrets from Keyvault
     */
    List<VersionedSecret> listSecrets();
}
