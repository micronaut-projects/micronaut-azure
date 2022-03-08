/*
 * Copyright 2017-2022 original authors
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
package io.micronaut.azure.credentials;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.env.Environment;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.Toggleable;

import java.util.Optional;

/**
 * The azure SDK configuration.
 *
 * @author Pavol Gressa
 * @since 3.1
 */
@ConfigurationProperties(AzureCredentialsConfiguration.PREFIX)
public interface AzureCredentialsConfiguration {
    String PREFIX = Environment.AZURE + ".credential";


    /**
     * The client certificate credential configuration.
     */
    @ConfigurationProperties(ClientCertificateCredentialConfiguration.NAME)
    interface ClientCertificateCredentialConfiguration {
        String NAME = "client-certificate";

        /**
         * Gets the client ID of the application.
         *
         * @return the client id
         */
        @NonNull
        String getClientId();

        /**
         * Gets the path of the PEM certificate for authenticating to AAD.
         *
         * @return the pem certificate path
         */
        Optional<String> getPemCertificatePath();

        /**
         * Gets the path of the PFX certificate for authenticating to AAD.
         *
         * @return the pfx certificate path
         */
        Optional<String> getPfxCertificatePath();

        /**
         * Gets the password protecting the PFX certificate file.
         *
         * @return the pfx certificate password
         */
        @Nullable
        String getPfxCertificatePassword();

        /**
         * Gets the tenant ID of the application.
         *
         * @return tenant id
         */
        @NonNull
        String getTenantId();
    }

    /**
     * The client secret credential configuration.
     */
    @ConfigurationProperties(ClientSecretCredentialConfiguration.NAME)
    interface ClientSecretCredentialConfiguration {
        String NAME = "client-secret";
        String CLIENT_SECRET = AzureCredentialsConfiguration.PREFIX + "." + NAME + "." + "secret";

        /**
         * Gets the client ID of the application.
         *
         * @return the client id
         */
        @NonNull
        String getClientId();

        /**
         * Gets the client secret for the authentication.
         *
         * @return client secret
         */
        @NonNull
        String getSecret();

        /**
         * Gets the tenant ID of the application.
         *
         * @return tenant id
         */
        @NonNull
        String getTenantId();
    }

    /**
     * The username password credential configuration.
     */
    @ConfigurationProperties(UsernamePasswordCredentialConfiguration.NAME)
    interface UsernamePasswordCredentialConfiguration {
        String NAME = "username-password";
        String USERNAME = AzureCredentialsConfiguration.PREFIX + "." + NAME + "." + "username";
        String PASSWORD = AzureCredentialsConfiguration.PREFIX + "." + NAME + "." + "password";

        /**
         * Gets the client ID of the application.
         *
         * @return the client id
         */
        @NonNull
        String getClientId();

        /**
         * Gets the username of the user.
         *
         * @return the username
         */
        @NonNull
        String getUsername();

        /**
         * Gets the password of the user.
         *
         * @return the password
         */
        @NonNull
        String getPassword();

        /**
         * Gets the tenant ID of the application.
         *
         * @return tenant id
         */
        Optional<String> getTenantId();
    }

    /**
     * The managed identity credential configuration.
     */
    @ConfigurationProperties(ManagedIdentityCredentialConfiguration.NAME)
    interface ManagedIdentityCredentialConfiguration extends Toggleable {
        String NAME = "managed-identity";
        String ENABLED = AzureCredentialsConfiguration.PREFIX + "." + NAME + "." + "enabled";

        /**
         * Specifies the client ID of user assigned or system assigned identity. Required only for user-assigned identity.
         *
         * @return client id
         */
        Optional<String> getClientId();
    }

    /**
     * The Azure CLI credential configuration.
     */
    @ConfigurationProperties(AzureCliCredentialConfiguration.NAME)
    interface AzureCliCredentialConfiguration extends Toggleable {
        String NAME = "cli";
        String ENABLED = AzureCredentialsConfiguration.PREFIX + "." + NAME + "." + "enabled";
    }

    /**
     * The IntelliJ credential configuration.
     */
    @ConfigurationProperties(IntelliJCredentialConfiguration.NAME)
    interface IntelliJCredentialConfiguration extends Toggleable {
        String NAME = "intellij";
        String ENABLED = AzureCredentialsConfiguration.PREFIX + "." + NAME + "." + "enabled";

        /**
         * The tenant id. The default is the tenant the user originally authenticated to via the Azure Toolkit
         * for IntelliJ plugin.
         *
         * @return tenant id
         */
        Optional<String> getTenantId();

        /**
         * Specifies the KeePass database path to read the cached credentials of Azure toolkit for IntelliJ plugin.
         * The databasePath is required on Windows platform. For macOS and Linux platform native key chain / key ring
         * will be accessed respectively to retrieve the cached credentials.
         *
         * @return path to KeePass database
         */
        Optional<String> getKeePassDatabasePath();
    }

    /**
     * The Visual studion code credential configuration.
     */
    @ConfigurationProperties(VisualStudioCodeCredentialConfiguration.NAME)
    interface VisualStudioCodeCredentialConfiguration extends Toggleable {
        String NAME = "visual-studio-code";
        String ENABLED = AzureCredentialsConfiguration.PREFIX + "." + NAME + "." + "enabled";

        /**
         * The tenant id. The default is the tenant the user originally authenticated to via the Visual Studio
         * Code Azure Account plugin.
         *
         * @return tenant id
         */
        Optional<String> getTenantId();
    }
}
