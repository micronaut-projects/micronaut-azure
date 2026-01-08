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
package io.micronaut.azure.secretmanager.signing;

import com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm;
import io.micronaut.core.annotation.NonNull;

/**
 * Service for signing payloads with Azure Key Vault keys.
 */
public interface KeyVaultKeySigner {

    /**
     * Sign the supplied data with the provided key using the specified algorithm.
     *
     * @param keyName   the key name
     * @param algorithm the signature algorithm
     * @param data      the data to sign
     * @return the signature bytes
     */
    @NonNull
    byte[] sign(@NonNull String keyName, @NonNull SignatureAlgorithm algorithm, @NonNull byte[] data);

    /**
     * Sign the supplied data with the provided key using the default algorithm configured for signing.
     *
     * @param keyName the key name
     * @param data    the data to sign
     * @return the signature bytes
     */
    @NonNull
    byte[] sign(@NonNull String keyName, @NonNull byte[] data);
}
