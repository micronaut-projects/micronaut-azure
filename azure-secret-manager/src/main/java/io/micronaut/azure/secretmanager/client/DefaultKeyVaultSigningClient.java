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

import com.azure.core.credential.TokenCredential;
import com.azure.security.keyvault.keys.cryptography.CryptographyClient;
import com.azure.security.keyvault.keys.cryptography.CryptographyClientBuilder;
import com.azure.security.keyvault.keys.cryptography.models.SignResult;
import com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm;
import io.micronaut.azure.secretmanager.configuration.AzureKeyVaultConfigurationProperties;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.core.util.StringUtils;
import jakarta.inject.Singleton;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Default implementation that delegates to {@link CryptographyClient}.
 *
 * <p>Maintains a bounded LRU cache of {@link CryptographyClient} instances to avoid
 * creating a new client for every signing operation while preventing unbounded memory growth.</p>
 */
@Singleton
@BootstrapContextCompatible
@Requires(classes = CryptographyClient.class)
@Requires(property = AzureKeyVaultConfigurationProperties.PREFIX + ".keys.enabled", value = StringUtils.TRUE)
@Requires(property = AzureKeyVaultConfigurationProperties.PREFIX + ".keys.signing.enabled", value = StringUtils.TRUE)
public class DefaultKeyVaultSigningClient implements KeyVaultSigningClient {

    private static final int MAX_CACHED_CLIENTS = 100;

    private final TokenCredential tokenCredential;
    private final Map<String, CryptographyClient> clients;

    /**
     * @param tokenCredential Azure token credentials
     */
    public DefaultKeyVaultSigningClient(TokenCredential tokenCredential) {
        this.tokenCredential = tokenCredential;
        // Bounded LRU cache - removes eldest entry when size exceeds MAX_CACHED_CLIENTS
        this.clients = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, CryptographyClient> eldest) {
                return size() > MAX_CACHED_CLIENTS;
            }
        };
    }

    @Override
    public byte[] sign(@NonNull String keyId, @NonNull SignatureAlgorithm algorithm, @NonNull byte[] data) {
        ArgumentUtils.requireNonNull("keyId", keyId);
        ArgumentUtils.requireNonNull("algorithm", algorithm);
        ArgumentUtils.requireNonNull("data", data);
        if (StringUtils.isEmpty(keyId)) {
            throw new IllegalArgumentException("keyId cannot be blank");
        }

        CryptographyClient client = getOrCreateClient(keyId);
        SignResult result = client.signData(algorithm, data);
        return result.getSignature();
    }

    private synchronized CryptographyClient getOrCreateClient(String keyId) {
        return clients.computeIfAbsent(keyId, this::buildClient);
    }

    private CryptographyClient buildClient(String keyId) {
        return new CryptographyClientBuilder()
                .keyIdentifier(keyId)
                .credential(tokenCredential)
                .buildClient();
    }
}
