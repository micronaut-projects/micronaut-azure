/*
 * Copyright 2017-2026 original authors
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
package io.micronaut.azure.secretmanager;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.AzureCliCredentialBuilder;
import com.azure.identity.ClientCertificateCredentialBuilder;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.identity.EnvironmentCredentialBuilder;
import com.azure.identity.IntelliJCredentialBuilder;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.identity.UsernamePasswordCredentialBuilder;
import com.azure.identity.VisualStudioCodeCredentialBuilder;
import io.micronaut.azure.secretmanager.client.DefaultSecretKeyVaultClient;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.context.env.Environment;
import io.micronaut.context.env.PropertySource;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.value.ConvertibleValues;
import io.micronaut.core.util.ConnectionString;
import io.micronaut.discovery.config.RetryablePropertySourceImporter;
import io.micronaut.retry.RetryPolicy;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Optional;

@Internal
final class AzureKeyVaultPropertySourceImporter extends RetryablePropertySourceImporter<AzureKeyVaultImportSettings> {

    static final String PROVIDER = "azure-key-vault";

    private final Map<CacheKey, DefaultSecretKeyVaultClient> clients = new ConcurrentHashMap<>();

    @Override
    public String getProvider() {
        return PROVIDER;
    }

    @Override
    protected AzureKeyVaultImportSettings newImportDeclaration(ConnectionString connectionString, RetryPolicy retryPolicy) {
        if (!PROVIDER.equals(connectionString.getProtocol())) {
            throw new IllegalArgumentException("Unsupported Azure Key Vault import protocol: " + connectionString.getProtocol());
        }
        return AzureKeyVaultImportSettings.fromConnectionString(connectionString);
    }

    @Override
    protected AzureKeyVaultImportSettings newImportDeclaration(ConvertibleValues<Object> values, RetryPolicy retryPolicy) {
        return AzureKeyVaultImportSettings.fromValues(values);
    }

    @Override
    protected Optional<PropertySource> importRetryablePropertySource(ImportContext<AzureKeyVaultImportSettings> context) {
        AzureKeyVaultImportSettings settings = merge(context.importDeclaration(), context.environment());
        DefaultSecretKeyVaultClient client = clients.computeIfAbsent(CacheKey.of(settings), k -> new DefaultSecretKeyVaultClient(
                AzureSecretManagerSupport.secretClient(settings.vaultUrl(), tokenCredential(settings))
        ));
        Map<String, Object> secrets = AzureKeyVaultPropertySourceMaterializer.materialize(
                client.listSecrets()
        );
        return Optional.of(PropertySource.of(context.getCanonicalLocation(), secrets));
    }

    @Override
    protected void closeRetryableImporter() {
        clients.values().forEach(this::closeClient);
        clients.clear();
    }

    private void closeClient(DefaultSecretKeyVaultClient client) {
        if (client instanceof AutoCloseable closeable) {
            try {
                closeable.close();
            } catch (Exception e) {
                throw new IllegalStateException("Error closing Azure Key Vault client", e);
            }
        }
    }

    private AzureKeyVaultImportSettings merge(AzureKeyVaultImportSettings declaration, Environment environment) {
        if (declaration != null && declaration.vaultUrl() != null && !declaration.vaultUrl().isBlank()) {
            return declaration;
        }
        return AzureKeyVaultImportSettings.resolve(environment);
    }

    private TokenCredential tokenCredential(AzureKeyVaultImportSettings settings) {
        return switch (settings.credentialMode()) {
            case "client-secret" -> clientSecretCredential(settings);
            case "client-certificate" -> clientCertificateCredential(settings);
            case "username-password" -> usernamePasswordCredential(settings);
            case "managed-identity" -> managedIdentityCredential(settings);
            case "environment" -> new EnvironmentCredentialBuilder().build();
            case "cli" -> new AzureCliCredentialBuilder().build();
            case "intellij" -> intelliJCredential(settings);
            case "visual-studio-code" -> visualStudioCodeCredential(settings);
            case "default" -> new DefaultAzureCredentialBuilder().build();
            default -> throw new ConfigurationException("Unsupported Azure Key Vault credential-mode: " + settings.credentialMode());
        };
    }

    private TokenCredential clientSecretCredential(AzureKeyVaultImportSettings settings) {
        if (isBlank(settings.clientId()) || isBlank(settings.tenantId()) || isBlank(settings.clientSecret())) {
            throw new ConfigurationException("Azure Key Vault import with credential-mode=client-secret requires client-id, tenant-id and client-secret options");
        }
        return new ClientSecretCredentialBuilder()
                .clientId(settings.clientId())
                .tenantId(settings.tenantId())
                .clientSecret(settings.clientSecret())
                .build();
    }

    private TokenCredential clientCertificateCredential(AzureKeyVaultImportSettings settings) {
        if (isBlank(settings.clientId()) || isBlank(settings.tenantId()) || isBlank(settings.certificatePath())) {
            throw new ConfigurationException("Azure Key Vault import with credential-mode=client-certificate requires client-id, tenant-id and certificate-path options");
        }
        ClientCertificateCredentialBuilder builder = new ClientCertificateCredentialBuilder()
                .clientId(settings.clientId())
                .tenantId(settings.tenantId());
        if (settings.certificatePath().endsWith(".pfx")) {
            builder.pfxCertificate(settings.certificatePath(), settings.certificatePassword());
        } else {
            builder.pemCertificate(settings.certificatePath());
        }
        return builder.build();
    }

    private TokenCredential usernamePasswordCredential(AzureKeyVaultImportSettings settings) {
        if (isBlank(settings.username()) || isBlank(settings.password()) || isBlank(settings.clientId())) {
            throw new ConfigurationException("Azure Key Vault import with credential-mode=username-password requires username/password user-info (or username/password options) and client-id");
        }
        UsernamePasswordCredentialBuilder builder = new UsernamePasswordCredentialBuilder()
                .username(settings.username())
                .password(settings.password())
                .clientId(settings.clientId());
        if (!isBlank(settings.tenantId())) {
            builder.tenantId(settings.tenantId());
        }
        return builder.build();
    }

    private TokenCredential managedIdentityCredential(AzureKeyVaultImportSettings settings) {
        ManagedIdentityCredentialBuilder builder = new ManagedIdentityCredentialBuilder();
        if (!isBlank(settings.managedIdentityClientId())) {
            builder.clientId(settings.managedIdentityClientId());
        }
        return builder.build();
    }

    private TokenCredential intelliJCredential(AzureKeyVaultImportSettings settings) {
        IntelliJCredentialBuilder builder = new IntelliJCredentialBuilder();
        if (!isBlank(settings.tenantId())) {
            builder.tenantId(settings.tenantId());
        }
        return builder.build();
    }

    private TokenCredential visualStudioCodeCredential(AzureKeyVaultImportSettings settings) {
        VisualStudioCodeCredentialBuilder builder = new VisualStudioCodeCredentialBuilder();
        if (!isBlank(settings.tenantId())) {
            builder.tenantId(settings.tenantId());
        }
        return builder.build();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * Cache key that excludes sensitive fields so secrets are not kept strongly referenced
     * as map keys for the lifetime of the importer.
     *
     * @param vaultUrl the vault URL
     * @param credentialMode the credential mode
     * @param clientId the client ID
     * @param tenantId the tenant ID
     * @param username the username
     * @param certificatePath the certificate path
     * @param managedIdentityClientId the managed identity client ID
     */
    private record CacheKey(String vaultUrl,
                            String credentialMode,
                            String clientId,
                            String tenantId,
                            String username,
                            String certificatePath,
                            String managedIdentityClientId) {

        static CacheKey of(AzureKeyVaultImportSettings settings) {
            return new CacheKey(settings.vaultUrl(), settings.credentialMode(), settings.clientId(),
                    settings.tenantId(), settings.username(), settings.certificatePath(), settings.managedIdentityClientId());
        }
    }
}
