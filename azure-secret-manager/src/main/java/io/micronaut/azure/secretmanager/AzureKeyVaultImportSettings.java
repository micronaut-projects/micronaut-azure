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

import io.micronaut.azure.secretmanager.configuration.AzureKeyVaultConfigurationProperties;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.context.env.Environment;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.value.ConvertibleValues;
import io.micronaut.core.util.ConnectionString;
import io.micronaut.core.util.StringUtils;

@Internal
record AzureKeyVaultImportSettings(String vaultUrl,
                                   String credentialMode,
                                   String clientId,
                                   String tenantId,
                                   String clientSecret,
                                   String username,
                                   String password,
                                   String certificatePath,
                                   String certificatePassword,
                                   String managedIdentityClientId) {

    private static final String VAULT_URL = AzureKeyVaultConfigurationProperties.PREFIX + ".vault-url";
    private static final String VAULT_URL_CAMEL = AzureKeyVaultConfigurationProperties.PREFIX + ".vaultUrl";
    private static final String CREDENTIAL_MODE = "credential-mode";
    private static final String CLIENT_ID = "client-id";
    private static final String TENANT_ID = "tenant-id";
    private static final String CLIENT_SECRET = "client-secret";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String CERTIFICATE_PATH = "certificate-path";
    private static final String CERTIFICATE_PASSWORD = "certificate-password";
    private static final String MANAGED_IDENTITY_CLIENT_ID = "managed-identity-client-id";

    static AzureKeyVaultImportSettings fromConnectionString(ConnectionString connectionString) {
        String vaultUrl = toVaultUrl(connectionString.getPath());
        return new AzureKeyVaultImportSettings(
                vaultUrl,
                connectionString.getOptions().getOrDefault(CREDENTIAL_MODE, "default"),
                connectionString.getOptions().get(CLIENT_ID),
                connectionString.getOptions().get(TENANT_ID),
                connectionString.getOptions().get(CLIENT_SECRET),
                connectionString.getUsername().orElse(connectionString.getOptions().get(USERNAME)),
                connectionString.getPassword().orElse(connectionString.getOptions().get(PASSWORD)),
                connectionString.getOptions().get(CERTIFICATE_PATH),
                connectionString.getOptions().get(CERTIFICATE_PASSWORD),
                connectionString.getOptions().get(MANAGED_IDENTITY_CLIENT_ID)
        );
    }

    static AzureKeyVaultImportSettings fromValues(ConvertibleValues<Object> values) {
        String vaultUrl = stringValue(values, "vault-url");
        if (!StringUtils.hasText(vaultUrl)) {
            vaultUrl = toVaultUrl(stringValue(values, "name"));
        }
        return new AzureKeyVaultImportSettings(
                vaultUrl,
                defaultString(stringValue(values, CREDENTIAL_MODE), "default"),
                stringValue(values, CLIENT_ID),
                stringValue(values, TENANT_ID),
                stringValue(values, CLIENT_SECRET),
                stringValue(values, USERNAME),
                stringValue(values, PASSWORD),
                stringValue(values, CERTIFICATE_PATH),
                stringValue(values, CERTIFICATE_PASSWORD),
                stringValue(values, MANAGED_IDENTITY_CLIENT_ID)
        );
    }


    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof AzureKeyVaultImportSettings that)) {
            return false;
        }
        return java.util.Objects.equals(vaultUrl, that.vaultUrl)
                && java.util.Objects.equals(credentialMode, that.credentialMode)
                && java.util.Objects.equals(clientId, that.clientId)
                && java.util.Objects.equals(tenantId, that.tenantId)
                && java.util.Objects.equals(username, that.username)
                && java.util.Objects.equals(certificatePath, that.certificatePath)
                && java.util.Objects.equals(managedIdentityClientId, that.managedIdentityClientId);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(vaultUrl, credentialMode, clientId, tenantId, username, certificatePath, managedIdentityClientId);
    }

    static AzureKeyVaultImportSettings resolve(Environment environment) {
        String vaultUrl = environment.getProperty(VAULT_URL, String.class)
                .orElseGet(() -> environment.getProperty(VAULT_URL_CAMEL, String.class).orElse(null));
        if (!StringUtils.hasText(vaultUrl)) {
            throw new ConfigurationException("Missing required Azure Key Vault configuration property: azure.key-vault.vault-url");
        }
        return new AzureKeyVaultImportSettings(vaultUrl, "default", null, null, null, null, null, null, null, null);
    }

    private static String toVaultUrl(String path) {
        if (!StringUtils.hasText(path)) {
            return null;
        }
        if (path.startsWith("https://") || path.startsWith("http://")) {
            return path;
        }
        return "https://" + path + ".vault.azure.net";
    }

    private static String stringValue(ConvertibleValues<Object> values, String name) {
        return values.get(name, String.class).orElse(null);
    }

    private static String defaultString(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }
}
