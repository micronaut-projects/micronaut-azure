package io.micronaut.azure.secretmanager

import com.azure.identity.DefaultAzureCredentialBuilder
import com.azure.security.keyvault.secrets.SecretClient
import spock.lang.Specification

class AzureSecretManagerSupportSpec extends Specification {

    void "it creates a secret client without bean lookup assumptions"() {
        given:
        def credential = new DefaultAzureCredentialBuilder().build()

        when:
        SecretClient client = AzureSecretManagerSupport.secretClient('https://example-vault.vault.azure.net/', credential)

        then:
        client != null
    }
}
