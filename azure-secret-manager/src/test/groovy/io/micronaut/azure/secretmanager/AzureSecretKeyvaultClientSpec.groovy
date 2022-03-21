package io.micronaut.azure.secretmanager

import io.micronaut.azure.secretmanager.client.DefaultSecretKeyvaultClient
import io.micronaut.azure.secretmanager.client.SecretKeyvaultClient
import io.micronaut.azure.secretmanager.client.VersionedSecret
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.BootstrapContextCompatible
import io.micronaut.context.annotation.Replaces
import io.micronaut.context.annotation.Requires
import io.micronaut.context.env.PropertySource
import jakarta.inject.Singleton
import reactor.core.publisher.Flux
import spock.lang.Specification

class AzureSecretKeyvaultClientSpec extends Specification {

    void "it loads secret from mocked vault"() {
        given:
        ApplicationContext ctx = ApplicationContext.run([
                'spec.name'                      : 'it tests AzureVaultConfigurationClient',
                "azure.keyvault.vaultUrl"        : "https://example-vault.azure.com",
                'micronaut.config-client.enabled': true
        ])
        def client = ctx.getBean(AzureVaultConfigurationClient.class)

        when:
        PropertySource propertySource = Flux.from(client.getPropertySources(null)).blockFirst()

        then:
        !propertySource.isEmpty()
        propertySource.get("secret_name") == "secretValue"
        propertySource.get("secret-name") == "secretValue"
        propertySource.get("secret.name") == "secretValue"
        propertySource.get('notFound') == null

        cleanup:
        ctx.close()
    }

    @Singleton
    @Replaces(DefaultSecretKeyvaultClient)
    @BootstrapContextCompatible
    @Requires(property = 'spec.name', value = 'it tests AzureVaultConfigurationClient')
    static class MockDefaultSecretKeyvaultClient implements SecretKeyvaultClient {

        @Override
        VersionedSecret getSecret(String secretName) {
            return null
        }

        @Override
        List<VersionedSecret> listSecrets() {
            return [new VersionedSecret("secret-name", "secretValue", "")]
        }
    }
}


