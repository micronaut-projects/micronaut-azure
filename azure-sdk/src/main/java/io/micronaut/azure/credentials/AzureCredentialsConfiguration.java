package io.micronaut.azure.credentials;

import io.micronaut.azure.AzureConfiguration;
import io.micronaut.context.annotation.ConfigurationProperties;

/**
 * @author Pavol Gressa
 * @since 2.5
 */
@ConfigurationProperties(AzureCredentialsConfiguration.PREFIX)
public interface AzureCredentialsConfiguration {
    String PREFIX = AzureConfiguration.PREFIX + ".credentials";


    @ConfigurationProperties("client-secret")
    interface ClientSecretCredentialConfiguration{
        String getClientSecret();
        String getTenantId();
    }

    @ConfigurationProperties("client-certificate")
    interface ClientCertificateCredentialConfiguration{
        String getPemCertificatePath();
        String getTenantId();
    }

    @ConfigurationProperties("username-password")
    interface UsernamePasswordCredentialConfiguration{
        String getUsername();
        String getPassword();
    }

    @ConfigurationProperties("managed-identity")
    interface ManagedIdentityCredentialConfiguration{
        String getClientId();
    }

    @ConfigurationProperties("default")
    interface ManagedIdentityCredentialConfiguration{

    }



}
