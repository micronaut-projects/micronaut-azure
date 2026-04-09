package io.micronaut.azure.secretmanager

import io.micronaut.core.util.ConnectionString
import io.micronaut.discovery.config.RetryablePropertySourceImporter
import io.micronaut.retry.RetryPolicy
import spock.lang.Specification

class AzureKeyVaultPropertySourceImporterSpec extends Specification {

    void "it accepts azure key vault connection string declarations"() {
        given:
        def importer = new AzureKeyVaultPropertySourceImporter()

        when:
        def declaration = importer.newImportDeclaration(ConnectionString.parse('azure-key-vault://contoso-vault2'))

        then:
        declaration != null
        declaration.declaration().vaultUrl() == 'https://contoso-vault2.vault.azure.net'
        declaration.declaration().credentialMode() == 'default'
        declaration.retryPolicy().maxAttempts() == RetryPolicy.DEFAULT_MAX_ATTEMPTS
    }

    void "it accepts credential customization options"() {
        given:
        def importer = new AzureKeyVaultPropertySourceImporter()

        when:
        def declaration = importer.newImportDeclaration(ConnectionString.parse('azure-key-vault://contoso-vault2?credential-mode=client-secret&client-id=client&tenant-id=tenant&client-secret=secret'))

        then:
        declaration.declaration().vaultUrl() == 'https://contoso-vault2.vault.azure.net'
        declaration.declaration().credentialMode() == 'client-secret'
        declaration.declaration().clientId() == 'client'
        declaration.declaration().tenantId() == 'tenant'
        declaration.declaration().clientSecret() == 'secret'
    }

    void "it maps user info to username password settings"() {
        given:
        def importer = new AzureKeyVaultPropertySourceImporter()

        when:
        def declaration = importer.newImportDeclaration(ConnectionString.parse('azure-key-vault://alice:secret@localhost/contoso-vault2?credential-mode=username-password&client-id=client&tenant-id=tenant'))

        then:
        declaration.declaration().vaultUrl() == 'https://contoso-vault2.vault.azure.net'
        declaration.declaration().credentialMode() == 'username-password'
        declaration.declaration().username() == 'alice'
        declaration.declaration().password() == 'secret'
        declaration.declaration().clientId() == 'client'
        declaration.declaration().tenantId() == 'tenant'
    }

    void "it accepts client certificate customization options"() {
        given:
        def importer = new AzureKeyVaultPropertySourceImporter()

        when:
        def declaration = importer.newImportDeclaration(ConnectionString.parse('azure-key-vault://contoso-vault2?credential-mode=client-certificate&client-id=client&tenant-id=tenant&certificate-path=/tmp/client.pem'))

        then:
        declaration.declaration().vaultUrl() == 'https://contoso-vault2.vault.azure.net'
        declaration.declaration().credentialMode() == 'client-certificate'
        declaration.declaration().clientId() == 'client'
        declaration.declaration().tenantId() == 'tenant'
        declaration.declaration().certificatePath() == '/tmp/client.pem'
    }

    void "it parses standard retry options"() {
        given:
        def importer = new AzureKeyVaultPropertySourceImporter()

        when:
        RetryablePropertySourceImporter.RetryableImportDeclaration<AzureKeyVaultImportSettings> declaration = importer.newImportDeclaration(
                ConnectionString.parse('azure-key-vault://contoso-vault2?retry-count=4&retry-attempts=5&retry-delay=250ms&retry-max-delay=3s&retry-multiplier=1.5&retry-jitter=0.2')
        )

        then:
        declaration.declaration().vaultUrl() == 'https://contoso-vault2.vault.azure.net'
        declaration.retryPolicy().maxAttempts() == 5
        declaration.retryPolicy().delay().toMillis() == 250
        declaration.retryPolicy().getMaxDelay().get().seconds == 3
        declaration.retryPolicy().multiplier() == 1.5d
        declaration.retryPolicy().jitter() == 0.2d
    }

    void "it parses standard retry options from map imports"() {
        given:
        def importer = new AzureKeyVaultPropertySourceImporter()

        when:
        RetryablePropertySourceImporter.RetryableImportDeclaration<AzureKeyVaultImportSettings> declaration = importer.newImportDeclaration(
                io.micronaut.core.convert.value.ConvertibleValues.of([
                        provider        : 'azure-key-vault',
                        name            : 'contoso-vault2',
                        'retry-attempts': 4,
                        'retry-delay'   : '100ms'
                ])
        )

        then:
        declaration.declaration().vaultUrl() == 'https://contoso-vault2.vault.azure.net'
        declaration.retryPolicy().maxAttempts() == 4
        declaration.retryPolicy().delay().toMillis() == 100
    }



    void "settings toString redacts secret-bearing fields"() {
        given:
        def settings = new AzureKeyVaultImportSettings('https://contoso-vault2.vault.azure.net', 'client-secret', 'client', 'tenant', 'secret-value', 'alice', 'password-value', '/tmp/cert.pem', 'certificate-password-value', 'managed-client')

        when:
        def rendered = settings.toString()

        then:
        rendered.contains('vaultUrl=https://contoso-vault2.vault.azure.net')
        rendered.contains('credentialMode=client-secret')
        rendered.contains('clientSecret=<redacted>')
        rendered.contains('password=<redacted>')
        rendered.contains('certificatePassword=<redacted>')
        !rendered.contains('secret-value')
        !rendered.contains('password-value')
        !rendered.contains('certificate-password-value')
    }

    void "settings equality ignores secret-bearing fields"() {
        given:
        def left = new AzureKeyVaultImportSettings('https://contoso-vault2.vault.azure.net', 'client-secret', 'client', 'tenant', 'secret-one', 'alice', 'password-one', '/tmp/cert.pem', 'cert-password-one', 'managed-client')
        def right = new AzureKeyVaultImportSettings('https://contoso-vault2.vault.azure.net', 'client-secret', 'client', 'tenant', 'secret-two', 'alice', 'password-two', '/tmp/cert.pem', 'cert-password-two', 'managed-client')

        expect:
        left == right
        left.hashCode() == right.hashCode()
    }

    void "settings equality changes when non-secret identity changes"() {
        given:
        def left = new AzureKeyVaultImportSettings('https://contoso-vault2.vault.azure.net', 'client-secret', 'client', 'tenant', 'secret', 'alice', 'password', '/tmp/cert.pem', 'cert-password', 'managed-client')
        def right = new AzureKeyVaultImportSettings('https://other-vault.vault.azure.net', 'client-secret', 'client', 'tenant', 'secret', 'alice', 'password', '/tmp/cert.pem', 'cert-password', 'managed-client')

        expect:
        left != right
    }

    void "it closes cached closeable clients before clearing the cache"() {
        given:
        def importer = new AzureKeyVaultPropertySourceImporter()
        def client = new CloseableDefaultSecretKeyVaultClient()
        Map cache = importer.@clients
        cache.put(new AzureKeyVaultImportSettings('https://contoso-vault2.vault.azure.net', 'default', null, null, null, null, null, null, null, null), client)

        when:
        importer.closeRetryableImporter()

        then:
        client.closed
        cache.isEmpty()
    }

    private static final class CloseableDefaultSecretKeyVaultClient extends io.micronaut.azure.secretmanager.client.DefaultSecretKeyVaultClient implements AutoCloseable {
        boolean closed

        CloseableDefaultSecretKeyVaultClient() {
            super(null)
        }

        @Override
        void close() {
            closed = true
        }
    }
}
