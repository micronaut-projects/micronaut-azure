/*
 * Copyright 2021 original authors
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
import com.azure.identity.AzureCliCredential;
import com.azure.identity.AzureCliCredentialBuilder;
import com.azure.identity.ClientCertificateCredential;
import com.azure.identity.ClientCertificateCredentialBuilder;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.identity.IntelliJCredential;
import com.azure.identity.IntelliJCredentialBuilder;
import com.azure.identity.ManagedIdentityCredential;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.identity.UsernamePasswordCredential;
import com.azure.identity.UsernamePasswordCredentialBuilder;
import com.azure.identity.VisualStudioCodeCredential;
import com.azure.identity.VisualStudioCodeCredentialBuilder;
import io.micronaut.azure.condition.ClientCertificateCredentialsCondition;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import jakarta.inject.Singleton;

/**
 * The factory creates the Azure SDK credentials based on the configuration {@link AzureCredentialsConfiguration}.
 *
 * @author Pavol Gressa
 * @since 3.1
 */
@Factory
public class AzureCredentialFactory {

    /**
     * Creates the {@link ClientCertificateCredential} builder.
     *
     * @param configuration the configuration
     * @return the builder
     */
    @Requires(condition = ClientCertificateCredentialsCondition.class)
    @Singleton
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
     * @return client certificate credentials
     * @see <a href="https://docs.microsoft.com/en-us/azure/developer/java/sdk/identity-service-principal-auth?view=azure-java-stable">identity-service-principal-auth</a>
     */
    @Requires(beans = ClientCertificateCredentialBuilder.class)
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
    public ClientSecretCredentialBuilder clientSecretCredentialBuilder(AzureCredentialsConfiguration.ClientSecretCredentialConfiguration configuration) {
        final ClientSecretCredentialBuilder builder = new ClientSecretCredentialBuilder();
        builder.clientSecret(configuration.getClientSecret());
        builder.clientId(configuration.getClientId());
        builder.tenantId(configuration.getTenantId());
        return builder;
    }

    /**
     * This credential authenticates the created service principal through its client secret (password).
     *
     * @param builder the builder
     * @return client secret credential
     * @see <a href="https://docs.microsoft.com/en-us/azure/developer/java/sdk/identity-service-principal-auth?view=azure-java-stable">identity-service-principal-auth</a>
     */
    @Requires(beans = ClientSecretCredentialBuilder.class)
    @Singleton
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
    public UsernamePasswordCredentialBuilder usernamePasswordCredentialBuilder(AzureCredentialsConfiguration.UsernamePasswordCredentialConfiguration configuration) {
        final UsernamePasswordCredentialBuilder builder = new UsernamePasswordCredentialBuilder();
        builder.username(configuration.getUsername());
        builder.password(configuration.getPassword());
        builder.clientId(configuration.getClientId());
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
    public ManagedIdentityCredential managedIdentityCredential(ManagedIdentityCredentialBuilder builder) {
        return builder.build();
    }

    /**
     * The {@link AzureCliCredential} builder.
     *
     * @return builder
     */
    @Requires(property = AzureCredentialsConfiguration.AzureCliCredentialConfiguration.ENABLED, notEquals = StringUtils.FALSE, defaultValue = StringUtils.FALSE)
    @Singleton
    public AzureCliCredentialBuilder azureCliCredentialBuilder() {
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
    public IntelliJCredentialBuilder intelliJCredentialBuilder(AzureCredentialsConfiguration.IntelliJCredentialConfiguration configuration) {
        final IntelliJCredentialBuilder builder = new IntelliJCredentialBuilder();
        configuration.getKeePassDatabasePath().ifPresent(builder::keePassDatabasePath);
        return builder;
    }

    /**
     * The Visual Studio Code credential enables authentication in development environments where
     * VS Code is installed with the VS Code Azure Account extension. It uses the logged-in user
     * information in the VS Code IDE and uses it to authenticate the application against Azure
     * Active Directory.
     *
     * @param builder the builder
     * @return intelli jidea credentials
     * @see <a href="https://docs.microsoft.com/en-us/azure/developer/java/sdk/identity-dev-env-auth?view=azure-java-stable#intellij-credential">IntelliJ credential</a>
     */
    @Requires(beans = IntelliJCredentialBuilder.class)
    @Singleton
    public IntelliJCredential intelliJCredential(IntelliJCredentialBuilder builder) {
        return builder.build();
    }

    /**
     * The {@link VisualStudioCodeCredential} builder.
     *
     * @return builder
     */
    @Requires(property = AzureCredentialsConfiguration.VisualStudioCodeCredentialConfiguration.ENABLED, notEquals = StringUtils.FALSE, defaultValue = StringUtils.FALSE)
    @Singleton
    public VisualStudioCodeCredentialBuilder visualStudioCodeCredentialBuilder(AzureCredentialsConfiguration.VisualStudioCodeCredentialConfiguration configuration) {
        final VisualStudioCodeCredentialBuilder visualStudioCodeCredentialBuilder = new VisualStudioCodeCredentialBuilder();
        configuration.getTenantId().ifPresent(visualStudioCodeCredentialBuilder::tenantId);
        return visualStudioCodeCredentialBuilder;
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
    public VisualStudioCodeCredential visualStudioCodeCredential(VisualStudioCodeCredentialBuilder builder) {
        return builder.build();
    }

    /**
     * The {@link DefaultAzureCredential} builder.
     *
     * @return builder
     */
    @Singleton
    public DefaultAzureCredentialBuilder defaultAzureCredentialBuilder() {
        return new DefaultAzureCredentialBuilder();
    }

    /**
     * The {@link DefaultAzureCredential} is appropriate for most scenarios where the application ultimately
     * runs in the Azure Cloud. {@link DefaultAzureCredential} combines credentials that are commonly used
     * to authenticate when deployed, with credentials that are used to authenticate in a
     * development environment.
     *
     * @return default azure credentials
     * @see <a href="https://docs.microsoft.com/en-us/azure/developer/java/sdk/identity-azure-hosted-auth?view=azure-java-stable#default-azure-credential">Default Azure credential</a>
     */
    @Requires(missingBeans = TokenCredential.class)
    public DefaultAzureCredential azureCredential() {
        return new DefaultAzureCredentialBuilder().build();
    }

}
