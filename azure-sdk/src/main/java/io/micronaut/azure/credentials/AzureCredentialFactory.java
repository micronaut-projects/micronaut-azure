/*
 * Copyright 2017-2021 original authors
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

import com.azure.core.credential.TokenCredential;
import com.azure.identity.*;
import com.azure.storage.common.StorageSharedKeyCredential;
import io.micronaut.azure.condition.ClientCertificateCredentialsCondition;
import io.micronaut.azure.condition.EnvironmentCredentialsCondition;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import jakarta.inject.Singleton;

import static io.micronaut.azure.credentials.AzureCredentialsConfiguration.StorageSharedKeyCredentialConfiguration.*;

/**
 * The factory creates the Azure SDK credentials based on the configuration {@link AzureCredentialsConfiguration}.
 *
 * @author Pavol Gressa
 * @since 3.1
 */
@Factory
@BootstrapContextCompatible
public class AzureCredentialFactory {

    /**
     * Creates the {@link ClientCertificateCredential} builder.
     *
     * @param configuration the configuration
     * @return the builder
     */
    @Requires(condition = ClientCertificateCredentialsCondition.class)
    @Singleton
    @BootstrapContextCompatible
    public ClientCertificateCredentialBuilder clientCertificateCredentialBuilder(AzureCredentialsConfiguration.ClientCertificateCredentialConfiguration configuration) {
        final ClientCertificateCredentialBuilder builder = new ClientCertificateCredentialBuilder();
        configuration.getPfxCertificatePath().ifPresent(s -> {
            builder.pfxCertificate(s, configuration.getPfxCertificatePassword());
        });

        configuration.getPemCertificatePath().ifPresent(builder::pemCertificate);
        builder.clientId(configuration.getClientId());
        builder.tenantId(configuration.getTenantId());
        return builder;
    }

    /**
     * This credential authenticates the created service principal through its client certificate.
     *
     * @param builder the builder
     * @return client certificate credentials
     * @see <a href="https://docs.microsoft.com/en-us/azure/developer/java/sdk/identity-service-principal-auth?view=azure-java-stable#client-certificate-credential">client-certificate-credential</a>
     */
    @Requires(beans = ClientCertificateCredentialBuilder.class)
    @Singleton
    @BootstrapContextCompatible
    public ClientCertificateCredential clientCertificateCredential(ClientCertificateCredentialBuilder builder) {
        return builder.build();
    }

    /**
     * Creates and configures the {@link ClientSecretCredential} builder.
     *
     * @param configuration the configuration
     * @return the builder
     */
    @Requires(property = AzureCredentialsConfiguration.ClientSecretCredentialConfiguration.CLIENT_SECRET)
    @Singleton
    @BootstrapContextCompatible
    public ClientSecretCredentialBuilder clientSecretCredentialBuilder(AzureCredentialsConfiguration.ClientSecretCredentialConfiguration configuration) {
        final ClientSecretCredentialBuilder builder = new ClientSecretCredentialBuilder();
        builder.clientSecret(configuration.getSecret());
        builder.clientId(configuration.getClientId());
        builder.tenantId(configuration.getTenantId());
        return builder;
    }

    /**
     * This credential authenticates the created service principal through its client secret (password).
     *
     * @param builder the builder
     * @return client secret credential
     * @see <a href="https://docs.microsoft.com/en-us/azure/developer/java/sdk/identity-service-principal-auth?view=azure-java-stable#client-secret-credential">client-secret-credential</a>
     */
    @Requires(beans = ClientSecretCredentialBuilder.class)
    @Singleton
    @BootstrapContextCompatible
    public ClientSecretCredential clientSecretCredential(ClientSecretCredentialBuilder builder) {
        return builder.build();
    }

    /**
     * Creates and configures the {@link UsernamePasswordCredential} builder.
     *
     * @param configuration the configuration
     * @return the builder
     */
    @Requires(property = AzureCredentialsConfiguration.UsernamePasswordCredentialConfiguration.USERNAME)
    @Requires(property = AzureCredentialsConfiguration.UsernamePasswordCredentialConfiguration.PASSWORD)
    @Singleton
    @BootstrapContextCompatible
    public UsernamePasswordCredentialBuilder usernamePasswordCredentialBuilder(AzureCredentialsConfiguration.UsernamePasswordCredentialConfiguration configuration) {
        final UsernamePasswordCredentialBuilder builder = new UsernamePasswordCredentialBuilder();
        builder.username(configuration.getUsername());
        builder.password(configuration.getPassword());
        builder.clientId(configuration.getClientId());
        configuration.getTenantId().ifPresent(builder::tenantId);
        return builder;
    }

    /**
     * The UsernamePasswordCredential helps to authenticate a public client application using the user credentials
     * that don't require multi-factor authentication.
     *
     * @param builder the builder
     * @return username password credential
     * @see <a href="https://docs.microsoft.com/en-us/azure/developer/java/sdk/identity-user-auth?view=azure-java-stable#username-password-credential">Username password credential</a>
     */

    @Requires(beans = UsernamePasswordCredentialBuilder.class)
    @Singleton
    @BootstrapContextCompatible
    public UsernamePasswordCredential usernamePasswordCredential(UsernamePasswordCredentialBuilder builder) {
        return builder.build();
    }

    /**
     * Creates the {@link ManagedIdentityCredential} builder.
     *
     * @param configuration the configuration
     * @return the builder
     */
    @Requires(property = AzureCredentialsConfiguration.ManagedIdentityCredentialConfiguration.ENABLED, notEquals = StringUtils.FALSE, defaultValue = StringUtils.FALSE)
    @Singleton
    @BootstrapContextCompatible
    public ManagedIdentityCredentialBuilder managedIdentityCredentialBuilder(AzureCredentialsConfiguration.ManagedIdentityCredentialConfiguration configuration) {
        final ManagedIdentityCredentialBuilder builder = new ManagedIdentityCredentialBuilder();
        configuration.getClientId().ifPresent(builder::clientId);
        return builder;
    }

    /**
     * The Managed Identity authenticates the managed identity (system or user assigned) of an Azure
     * resource. So, if the application is running inside an Azure resource that supports Managed
     * Identity through IDENTITY/MSI, IMDS endpoints, or both, then this credential will get your
     * application authenticated, and offers a great secretless authentication experience.
     *
     * @param builder the builder
     * @return managed identity credential
     * @see <a href="https://docs.microsoft.com/en-us/azure/developer/java/sdk/identity-azure-hosted-auth?view=azure-java-stable#managed-identity-credential">Managed Identity credential</a>
     */
    @Requires(beans = ManagedIdentityCredentialBuilder.class)
    @Singleton
    @BootstrapContextCompatible
    public ManagedIdentityCredential managedIdentityCredential(ManagedIdentityCredentialBuilder builder) {
        return builder.build();
    }

    /**
     * The {@link AzureCliCredential} builder.
     *
     * @param configuration the configuration
     * @return builder
     */
    @Requires(property = AzureCredentialsConfiguration.AzureCliCredentialConfiguration.ENABLED, notEquals = StringUtils.FALSE, defaultValue = StringUtils.FALSE)
    @Singleton
    @BootstrapContextCompatible
    public AzureCliCredentialBuilder azureCliCredentialBuilder(AzureCredentialsConfiguration.AzureCliCredentialConfiguration configuration) {
        return new AzureCliCredentialBuilder();
    }

    /**
     * The Azure CLI credential authenticates in a development environment with the enabled user
     * or service principal in Azure CLI. It uses the Azure CLI given a user that is already
     * logged into it, and uses the CLI to authenticate the application against Azure Active Directory.
     *
     * @param builder the builder
     * @return azure cli credentials
     * @see <a href="https://docs.microsoft.com/en-us/azure/developer/java/sdk/identity-dev-env-auth?view=azure-java-stable#azure-cli-credential">Azure CLI credential</a>
     */
    @Requires(beans = AzureCliCredentialBuilder.class)
    @Singleton
    @BootstrapContextCompatible
    public AzureCliCredential azureCliCredential(AzureCliCredentialBuilder builder) {
        return builder.build();
    }

    /**
     * The {@link IntelliJCredential} builder.
     *
     * @param configuration the configuration
     * @return builder
     */
    @Requires(property = AzureCredentialsConfiguration.IntelliJCredentialConfiguration.ENABLED, notEquals = StringUtils.FALSE, defaultValue = StringUtils.FALSE)
    @Singleton
    @BootstrapContextCompatible
    public IntelliJCredentialBuilder intelliJCredentialBuilder(AzureCredentialsConfiguration.IntelliJCredentialConfiguration configuration) {
        final IntelliJCredentialBuilder builder = new IntelliJCredentialBuilder();
        configuration.getKeePassDatabasePath().ifPresent(builder::keePassDatabasePath);
        configuration.getTenantId().ifPresent(builder::tenantId);
        return builder;
    }

    /**
     * The IntelliJ credential authenticates in a development environment with the account in Azure Toolkit for IntelliJ.
     * It uses the logged in user information on the IntelliJ IDE and uses it to authenticate the application against
     * Azure Active Directory.
     *
     * @param builder the builder
     * @return intellij idea credentials
     * @see <a href="https://docs.microsoft.com/en-us/azure/developer/java/sdk/identity-dev-env-auth?view=azure-java-stable#intellij-credential">IntelliJ credential</a>
     */
    @Requires(beans = IntelliJCredentialBuilder.class)
    @Singleton
    @BootstrapContextCompatible
    public IntelliJCredential intelliJCredential(IntelliJCredentialBuilder builder) {
        return builder.build();
    }

    /**
     * The {@link VisualStudioCodeCredential} builder.
     *
     * @param configuration the configuration
     * @return builder
     */
    @Requires(property = AzureCredentialsConfiguration.VisualStudioCodeCredentialConfiguration.ENABLED, notEquals = StringUtils.FALSE, defaultValue = StringUtils.FALSE)
    @Singleton
    @BootstrapContextCompatible
    public VisualStudioCodeCredentialBuilder visualStudioCodeCredentialBuilder(AzureCredentialsConfiguration.VisualStudioCodeCredentialConfiguration configuration) {
        final VisualStudioCodeCredentialBuilder builder = new VisualStudioCodeCredentialBuilder();
        configuration.getTenantId().ifPresent(builder::tenantId);
        return builder;
    }

    /**
     * The Visual Studio Code credential enables authentication in development environments where
     * VS Code is installed with the VS Code Azure Account extension. It uses the logged-in user
     * information in the VS Code IDE and uses it to authenticate the application against Azure
     * Active Directory.
     *
     * @param builder the builder
     * @return visual studio code credentials
     * @see <a href="https://docs.microsoft.com/en-us/azure/developer/java/sdk/identity-dev-env-auth?view=azure-java-stable#visual-studio-code-credential">Visual Studio Code credential</a>
     */
    @Requires(beans = VisualStudioCodeCredentialBuilder.class)
    @Singleton
    @BootstrapContextCompatible
    public VisualStudioCodeCredential visualStudioCodeCredential(VisualStudioCodeCredentialBuilder builder) {
        return builder.build();
    }

    /**
     * The {@link DefaultAzureCredential} builder.
     *
     * @return builder
     */
    @Singleton
    @BootstrapContextCompatible
    public DefaultAzureCredentialBuilder defaultAzureCredentialBuilder() {
        return new DefaultAzureCredentialBuilder();
    }

    /**
     * The {@link DefaultAzureCredential} is appropriate for most scenarios where the application ultimately
     * runs in the Azure Cloud. {@link DefaultAzureCredential} combines credentials that are commonly used
     * to authenticate when deployed, with credentials that are used to authenticate in a
     * development environment.
     *
     * @param builder the builder
     * @return default azure credentials
     * @see <a href="https://docs.microsoft.com/en-us/azure/developer/java/sdk/identity-azure-hosted-auth?view=azure-java-stable#default-azure-credential">Default Azure credential</a>
     */
    @Requires(missingBeans = TokenCredential.class)
    @Singleton
    @BootstrapContextCompatible
    public DefaultAzureCredential defaultAzureCredential(DefaultAzureCredentialBuilder builder) {
        return builder.build();
    }

    /**
     * Fluent credential builder for instantiating a {@link EnvironmentCredential}.
     *
     * @return the environment credential builder.
     */
    @Requires(condition = EnvironmentCredentialsCondition.class)
    @Singleton
    @BootstrapContextCompatible
    public EnvironmentCredentialBuilder environmentCredentialBuilder() {
        return new EnvironmentCredentialBuilder();
    }

    /**
     * <p>A credential provider that provides token credentials based on environment variables.
     * The environment variables expected are:</p>
     *
     * <ul>
     *     <li><code>AZURE_CLIENT_ID</code></li>
     *     <li><code>AZURE_CLIENT_SECRET</code></li>
     *     <li><code>AZURE_TENANT_ID</code></li>
     * </ul>
     * <p>or:</p>
     * <ul>
     *     <li><code>AZURE_CLIENT_ID</code></li>
     *     <li><code>AZURE_CLIENT_CERTIFICATE_PATH</code></li>
     *     <li><code>AZURE_TENANT_ID</code></li>
     * </ul>
     * <p>or:</p>
     * <ul>
     *     <li><code>AZURE_CLIENT_ID</code></li>
     *     <li><code>AZURE_USERNAME</code></li>
     *     <li><code>AZURE_PASSWORD</code></li>
     * </ul>
     *
     * @param builder the environment credential builder.
     * @return the environment credential.
     */
    @Requires(bean = EnvironmentCredentialBuilder.class)
    @Singleton
    @BootstrapContextCompatible
    public EnvironmentCredential environmentCredential(final EnvironmentCredentialBuilder builder) {
        return builder.build();
    }

    /**
     * Creates a {@link StorageSharedKeyCredential} from a connection string.
     *
     * @param configuration the configuration
     * @return a SharedKey credential policy that is put into a header to authorize requests.
     * @see <a href="https://docs.microsoft.com/en-us/java/api/com.azure.storage.common.storagesharedkeycredential?view=azure-java-stable">StorageSharedKeyCredential</a>
     */
    @Requires(property = CONNECTION_STRING)
    @Singleton
    @BootstrapContextCompatible
    public StorageSharedKeyCredential storageSharedKeyCredentialFromConnectionString(AzureCredentialsConfiguration.StorageSharedKeyCredentialConfiguration configuration) {
        return StorageSharedKeyCredential.fromConnectionString(configuration.getConnectionString().orElse(""));
    }

    /**
     * Creates a {@link StorageSharedKeyCredential} from an account name and key.
     *
     * @param configuration the configuration
     * @return a SharedKey credential policy that is put into a header to authorize requests.
     * @see <a href="https://docs.microsoft.com/en-us/java/api/com.azure.storage.common.storagesharedkeycredential?view=azure-java-stable">StorageSharedKeyCredential</a>
     */
    @Requires(property = ACCOUNT_NAME)
    @Requires(property = ACCOUNT_KEY)
    @Singleton
    @BootstrapContextCompatible
    public StorageSharedKeyCredential storageSharedKeyCredentialFromAccountNameAndKey(AzureCredentialsConfiguration.StorageSharedKeyCredentialConfiguration configuration) {
        return new StorageSharedKeyCredential(configuration.getAccountName().orElse(""), configuration.getAccountKey().orElse(""));
    }

}
