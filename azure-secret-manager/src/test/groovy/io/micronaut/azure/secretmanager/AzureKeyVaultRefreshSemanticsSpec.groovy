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

class AzureKeyVaultRefreshSemanticsSpec extends Specification {

    void "legacy property loading is a startup snapshot within one context"() {
        given:
        SnapshotMockDefaultSecretKeyVaultClient.currentSecrets = [new KeyVaultSecret('snapshot-secret', 'v1')]
        ApplicationContext ctx = ApplicationContext.run([
                'spec.name'                      : 'azure snapshot semantics',
                'azure.key-vault.vaultUrl'       : 'https://example-vault.azure.com',
                'micronaut.config-client.enabled': true
        ])
        def client = ctx.getBean(AzureVaultConfigurationClient)

        when:
        PropertySource first = Flux.from(client.getPropertySources(ctx.environment)).blockFirst()
        SnapshotMockDefaultSecretKeyVaultClient.currentSecrets = [new KeyVaultSecret('snapshot-secret', 'v2')]

        then:
        first.get('snapshot-secret') == 'v1'

        cleanup:
        ctx.close()
    }

    void "fresh context observes a fresh snapshot"() {
        when:
        SnapshotMockDefaultSecretKeyVaultClient.currentSecrets = [new KeyVaultSecret('snapshot-secret', 'v1')]
        ApplicationContext firstContext = ApplicationContext.run([
                'spec.name'                      : 'azure snapshot semantics',
                'azure.key-vault.vaultUrl'       : 'https://example-vault.azure.com',
                'micronaut.config-client.enabled': true
        ])
        String firstValue = Flux.from(firstContext.getBean(AzureVaultConfigurationClient).getPropertySources(firstContext.environment)).blockFirst().get('snapshot-secret')
        firstContext.close()

        SnapshotMockDefaultSecretKeyVaultClient.currentSecrets = [new KeyVaultSecret('snapshot-secret', 'v2')]
        ApplicationContext secondContext = ApplicationContext.run([
                'spec.name'                      : 'azure snapshot semantics',
                'azure.key-vault.vaultUrl'       : 'https://example-vault.azure.com',
                'micronaut.config-client.enabled': true
        ])
        String secondValue = Flux.from(secondContext.getBean(AzureVaultConfigurationClient).getPropertySources(secondContext.environment)).blockFirst().get('snapshot-secret')

        then:
        firstValue == 'v1'
        secondValue == 'v2'

        cleanup:
        secondContext.close()
    }

    @Singleton
    @Replaces(DefaultSecretKeyVaultClient)
    @BootstrapContextCompatible
    @Requires(property = 'spec.name', value = 'azure snapshot semantics')
    static class SnapshotMockDefaultSecretKeyVaultClient implements SecretKeyVaultClient {
        static List<KeyVaultSecret> currentSecrets = []

        @Override
        KeyVaultSecret getSecret(String secretName) {
            currentSecrets.find { it.name == secretName }
        }

        @Override
        List<KeyVaultSecret> listSecrets() {
            currentSecrets
        }
    }
}
