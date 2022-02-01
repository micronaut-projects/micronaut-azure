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

import com.azure.identity.ClientCertificateCredential;
import com.azure.identity.ClientCertificateCredentialBuilder;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.identity.ManagedIdentityCredential;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.identity.UsernamePasswordCredential;
import com.azure.identity.UsernamePasswordCredentialBuilder;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;

/**
 * @author Pavol Gressa
 * @since 2.5
 */
@Factory
public class AzureCredentialFactory {

    /**
     * Creates the {@link ClientCertificateCredentialBuilder}.
     *
     * @return builder
     */
    @Singleton
    public ClientCertificateCredentialBuilder clientCertificateCredentialBuilder(){
        return new ClientCertificateCredentialBuilder();
    }

    /**
     * This credential authenticates the created service principal through its client certificate.
     *
     * @see <a href="https://docs.microsoft.com/en-us/azure/developer/java/sdk/identity-service-principal-auth?view=azure-java-stable">identity-service-principal-auth</a>
     * @return client certificate credentials
     */
    public ClientCertificateCredential clientCertificateCredential(ClientCertificateCredentialBuilder builder){
        return builder
            .clientId("<your client ID>")
            .pemCertificate("<path to PEM certificate>")
            // Choose between either a PEM certificate or a PFX certificate.
            //.pfxCertificate("<path to PFX certificate>", "PFX CERTIFICATE PASSWORD")
            .tenantId("<your tenant ID>")
            .build();
    }

    /**
     * This credential authenticates the created service principal through its client secret (password).
     *
     * @see <a href="https://docs.microsoft.com/en-us/azure/developer/java/sdk/identity-service-principal-auth?view=azure-java-stable">identity-service-principal-auth</a>
     * @return configured client secret credential
     */
    public ClientSecretCredential clientSecretCredential(){
        return new ClientSecretCredentialBuilder()
            .clientId("<your client ID>")
            .clientSecret("<your client secret>")
            .tenantId("<your tenant ID>")
            .build();
    }

    /**
     * The UsernamePasswordCredential helps to authenticate a public client application using the user credentials that don't require multi-factor authentication.
     *
     * @see <a href="https://docs.microsoft.com/en-us/azure/developer/java/sdk/identity-user-auth?view=azure-java-stable">Username password credential</a>
     * @return username password credential
     */
    public UsernamePasswordCredential usernamePasswordCredential(){
        return new UsernamePasswordCredentialBuilder()
            .clientId("<your app client ID>")
            .username("<your username>")
            .password("<your password>")
            .build();
    }

    /**
     * The Managed Identity authenticates the managed identity (system or user assigned) of an Azure
     * resource. So, if the application is running inside an Azure resource that supports Managed
     * Identity through IDENTITY/MSI, IMDS endpoints, or both, then this credential will get your
     * application authenticated, and offers a great secretless authentication experience.
     *
     * @see <a href="https://docs.microsoft.com/en-us/azure/developer/java/sdk/identity-azure-hosted-auth?view=azure-java-stable#managed-identity-credential">Managed Identity credential</a>
     * @return managed identity credential
     */
    public ManagedIdentityCredential managedIdentityCredential(){
        return new ManagedIdentityCredentialBuilder()
            .clientId("<user-assigned managed identity client ID>") // required only for user-assigned
            .build();
    }

    /**
     * The {@link DefaultAzureCredential} is appropriate for most scenarios where the application ultimately
     * runs in the Azure Cloud. {@link DefaultAzureCredential} combines credentials that are commonly used
     * to authenticate when deployed, with credentials that are used to authenticate in a
     * development environment.
     *
     * @see <a href="https://docs.microsoft.com/en-us/azure/developer/java/sdk/identity-azure-hosted-auth?view=azure-java-stable#default-azure-credential">Default Azure credential</a>
     * @return default azure credentials
     */
    public DefaultAzureCredential azureCredential(){
        return new DefaultAzureCredentialBuilder().build();
    }

}
