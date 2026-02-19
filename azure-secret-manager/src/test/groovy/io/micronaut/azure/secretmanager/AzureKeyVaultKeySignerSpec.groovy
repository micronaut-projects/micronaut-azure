package io.micronaut.azure.secretmanager

import com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm
import io.micronaut.azure.secretmanager.client.DefaultKeyVaultSigningClient
import io.micronaut.azure.secretmanager.client.KeyVaultSigningClient
import io.micronaut.azure.secretmanager.signing.KeyVaultKeySigner
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.BootstrapContextCompatible
import io.micronaut.context.annotation.Replaces
import io.micronaut.context.annotation.Requires
import jakarta.inject.Singleton
import spock.lang.Specification

class AzureKeyVaultKeySignerSpec extends Specification {

    void "it signs payloads using provided algorithm"() {
        given:
        byte[] payload = "payload".bytes
        MockKeyVaultSigningClient.signature = "signed".bytes
        MockKeyVaultSigningClient.reset()

        ApplicationContext ctx = ApplicationContext.run([
                'spec.name'                                      : 'azure-key-signing',
                'azure.key-vault.vault-url'                      : 'https://example-vault.vault.azure.net',
                'azure.key-vault.keys.enabled'                   : true,
                'azure.key-vault.keys.signing.enabled'           : true,
                'azure.key-vault.keys.signing.default-algorithm' : 'RS256'
        ])
        KeyVaultKeySigner signer = ctx.getBean(KeyVaultKeySigner)

        when:
        byte[] signature = signer.sign('sample-key', SignatureAlgorithm.RS256, payload)

        then:
        signature == MockKeyVaultSigningClient.signature
        MockKeyVaultSigningClient.lastKeyId == 'https://example-vault.vault.azure.net/keys/sample-key'
        MockKeyVaultSigningClient.lastAlgorithm == SignatureAlgorithm.RS256
        MockKeyVaultSigningClient.lastPayload == payload

        cleanup:
        ctx.close()
        MockKeyVaultSigningClient.reset()
    }

    void "it signs payloads using default algorithm"() {
        given:
        byte[] payload = "payload".bytes
        MockKeyVaultSigningClient.signature = "signed".bytes
        MockKeyVaultSigningClient.reset()

        ApplicationContext ctx = ApplicationContext.run([
                'spec.name'                                      : 'azure-key-signing',
                'azure.key-vault.vault-url'                      : 'https://example-vault.vault.azure.net',
                'azure.key-vault.keys.enabled'                   : true,
                'azure.key-vault.keys.signing.enabled'           : true,
                'azure.key-vault.keys.signing.default-algorithm' : 'RS256'
        ])
        KeyVaultKeySigner signer = ctx.getBean(KeyVaultKeySigner)

        when:
        byte[] signature = signer.sign('my-key', payload)

        then:
        signature == MockKeyVaultSigningClient.signature
        MockKeyVaultSigningClient.lastKeyId == 'https://example-vault.vault.azure.net/keys/my-key'
        MockKeyVaultSigningClient.lastAlgorithm == SignatureAlgorithm.RS256

        cleanup:
        ctx.close()
        MockKeyVaultSigningClient.reset()
    }

    void "it requires a default algorithm when none supplied"() {
        given:
        MockKeyVaultSigningClient.signature = "signed".bytes
        MockKeyVaultSigningClient.reset()

        ApplicationContext ctx = ApplicationContext.run([
                'spec.name'                            : 'azure-key-signing',
                'azure.key-vault.vault-url'            : 'https://example-vault.vault.azure.net',
                'azure.key-vault.keys.enabled'         : true,
                'azure.key-vault.keys.signing.enabled' : true
        ])
        KeyVaultKeySigner signer = ctx.getBean(KeyVaultKeySigner)

        when:
        signer.sign('sample-key', "payload".bytes)

        then:
        thrown IllegalStateException

        cleanup:
        ctx.close()
        MockKeyVaultSigningClient.reset()
    }

    void "it normalizes vault URL with trailing slash"() {
        given:
        byte[] payload = "payload".bytes
        MockKeyVaultSigningClient.signature = "signed".bytes
        MockKeyVaultSigningClient.reset()

        ApplicationContext ctx = ApplicationContext.run([
                'spec.name'                                      : 'azure-key-signing',
                'azure.key-vault.vault-url'                      : 'https://example-vault.vault.azure.net/',
                'azure.key-vault.keys.enabled'                   : true,
                'azure.key-vault.keys.signing.enabled'           : true,
                'azure.key-vault.keys.signing.default-algorithm' : 'RS256'
        ])
        KeyVaultKeySigner signer = ctx.getBean(KeyVaultKeySigner)

        when:
        signer.sign('my-key', SignatureAlgorithm.RS256, payload)

        then:
        MockKeyVaultSigningClient.lastKeyId == 'https://example-vault.vault.azure.net/keys/my-key'

        cleanup:
        ctx.close()
        MockKeyVaultSigningClient.reset()
    }

    @Singleton
    @Replaces(DefaultKeyVaultSigningClient)
    @BootstrapContextCompatible
    @Requires(property = 'spec.name', value = 'azure-key-signing')
    static class MockKeyVaultSigningClient implements KeyVaultSigningClient {

        static byte[] signature = "signed".bytes
        static String lastKeyId
        static SignatureAlgorithm lastAlgorithm
        static byte[] lastPayload

        static void reset() {
            lastKeyId = null
            lastAlgorithm = null
            lastPayload = null
        }

        @Override
        byte[] sign(String keyId, SignatureAlgorithm algorithm, byte[] data) {
            lastKeyId = keyId
            lastAlgorithm = algorithm
            lastPayload = data
            return signature
        }
    }
}
