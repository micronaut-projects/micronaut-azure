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
import io.micronaut.azure.secretmanager.client.KeyVaultSigningClient;
import io.micronaut.azure.secretmanager.configuration.AzureKeyVaultConfigurationProperties;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.core.util.StringUtils;
import jakarta.inject.Singleton;

/**
 * Default implementation of {@link KeyVaultKeySigner}.
 *
 * <p>Constructs key identifiers from the configured vault URL and key name,
 * delegating the actual signing operation to Azure Key Vault via {@link KeyVaultSigningClient}.</p>
 */
@Singleton
@BootstrapContextCompatible
@Requires(beans = KeyVaultSigningClient.class)
@Requires(property = AzureKeyVaultConfigurationProperties.PREFIX + ".keys.enabled", value = StringUtils.TRUE)
@Requires(property = AzureKeyVaultConfigurationProperties.PREFIX + ".keys.signing.enabled", value = StringUtils.TRUE)
public class DefaultKeyVaultKeySigner implements KeyVaultKeySigner {

    private final KeyVaultSigningClient keyVaultSigningClient;
    private final AzureKeyVaultConfigurationProperties configurationProperties;
    private final String vaultUrl;

    /**
     * @param keyVaultSigningClient    Signing client abstraction
     * @param configurationProperties  Configuration properties
     */
    public DefaultKeyVaultKeySigner(
            KeyVaultSigningClient keyVaultSigningClient,
            AzureKeyVaultConfigurationProperties configurationProperties
    ) {
        this.keyVaultSigningClient = keyVaultSigningClient;
        this.configurationProperties = configurationProperties;
        this.vaultUrl = normalizeVaultUrl(configurationProperties.getVaultURL());
    }

    @Override
    public byte[] sign(@NonNull String keyName, @NonNull SignatureAlgorithm algorithm, @NonNull byte[] data) {
        ArgumentUtils.requireNonNull("keyName", keyName);
        ArgumentUtils.requireNonNull("algorithm", algorithm);
        ArgumentUtils.requireNonNull("data", data);
        if (StringUtils.isEmpty(keyName)) {
            throw new IllegalArgumentException("keyName cannot be blank");
        }
        String keyId = buildKeyId(keyName);
        return keyVaultSigningClient.sign(keyId, algorithm, data);
    }

    @Override
    public byte[] sign(@NonNull String keyName, @NonNull byte[] data) {
        SignatureAlgorithm algorithm = resolveDefaultAlgorithm();
        if (algorithm == null) {
            throw new IllegalStateException(
                "No default signing algorithm configured. " +
                "Specify azure.key-vault.keys.signing.default-algorithm or provide an algorithm explicitly."
            );
        }
        return sign(keyName, algorithm, data);
    }

    /**
     * Builds a key identifier URL from the vault URL and key name.
     * Key Vault key identifiers follow the pattern: {vaultUrl}/keys/{keyName}
     *
     * @param keyName the key name
     * @return the full key identifier URL
     */
    private String buildKeyId(String keyName) {
        return vaultUrl + "/keys/" + keyName;
    }

    private String normalizeVaultUrl(String url) {
        if (url == null) {
            throw new IllegalStateException("Vault URL must be configured via azure.key-vault.vault-url");
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private SignatureAlgorithm resolveDefaultAlgorithm() {
        String configured = configurationProperties.getKeys().getSigning().getDefaultAlgorithm();
        if (StringUtils.isEmpty(configured)) {
            return null;
        }
        return SignatureAlgorithm.fromString(configured);
    }
}
