package io.micronaut.azure.secretmanager

import com.azure.core.credential.TokenCredential
import com.azure.identity.DefaultAzureCredentialBuilder
import com.azure.security.keyvault.secrets.SecretClient
import io.micronaut.azure.secretmanager.configuration.AzureKeyVaultConfigurationProperties
import spock.lang.Specification

class SecretManagerFactorySpec extends Specification {

    void "it delegates secret client creation to shared helper"() {
        given:
        TokenCredential credential = new DefaultAzureCredentialBuilder().build()
        AzureKeyVaultConfigurationProperties configuration = new AzureKeyVaultConfigurationProperties()
        configuration.setVaultURL('https://example-vault.vault.azure.net/')

        when:
        SecretClient client = new SecretManagerFactory().secretClient(credential, configuration)

        then:
        client != null
    }
}
