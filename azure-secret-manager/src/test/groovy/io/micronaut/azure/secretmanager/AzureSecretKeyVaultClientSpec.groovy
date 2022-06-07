package io.micronaut.azure.secretmanager

import com.azure.security.keyvault.secrets.models.KeyVaultSecret
import io.micronaut.azure.secretmanager.client.DefaultSecretKeyVaultClient
import io.micronaut.azure.secretmanager.client.SecretKeyVaultClient
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.BootstrapContextCompatible
import io.micronaut.context.annotation.Replaces
import io.micronaut.context.annotation.Requires
import io.micronaut.context.env.PropertySource
import jakarta.inject.Singleton
import reactor.core.publisher.Flux
import spock.lang.Specification

class AzureSecretKeyVaultClientSpec extends Specification {

    void "it loads secret from mocked vault"() {
        given:
        ApplicationContext ctx = ApplicationContext.run([
                'spec.name'                      : 'it tests AzureVaultConfigurationClient',
                "azure.key-vault.vaultUrl"       : "https://example-vault.azure.com",
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

    void "it tries to load from empty vault url"() {
        given:
        ApplicationContext ctx = ApplicationContext.run([
                'spec.name'                      : 'it tests AzureVaultConfigurationClient',
                "azure.key-vault.vaultUrl"       : "",
                'micronaut.config-client.enabled': true
        ])
        def client = ctx.getBean(AzureVaultConfigurationClient.class)

        when:
        PropertySource propertySource = Flux.from(client.getPropertySources(null)).blockFirst()

        then:
        propertySource == null

        cleanup:
        ctx.close()

    }

    @Singleton
    @Replaces(DefaultSecretKeyVaultClient)
    @BootstrapContextCompatible
    @Requires(property = 'spec.name', value = 'it tests AzureVaultConfigurationClient')
    static class MockDefaultSecretKeyVaultClient implements SecretKeyVaultClient {

        @Override
        KeyVaultSecret getSecret(String secretName) {
            return null
        }

        @Override
        List<KeyVaultSecret> listSecrets() {
            return [new KeyVaultSecret("secret-name", "secretValue")]
        }
    }

}


