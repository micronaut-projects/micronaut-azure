package io.micronaut.azure.secretmanager

import com.azure.identity.AzureCliCredential
import com.azure.identity.ClientCertificateCredential
import com.azure.identity.ClientSecretCredential
import com.azure.identity.DefaultAzureCredential
import com.azure.identity.EnvironmentCredential
import com.azure.identity.IntelliJCredential
import com.azure.identity.ManagedIdentityCredential
import com.azure.identity.UsernamePasswordCredential
import com.azure.identity.VisualStudioCodeCredential
import com.azure.security.keyvault.secrets.models.KeyVaultSecret
import io.micronaut.azure.secretmanager.client.DefaultSecretKeyVaultClient
import io.micronaut.context.env.Environment
import io.micronaut.context.env.PropertySource
import io.micronaut.context.env.PropertySourceImporter
import io.micronaut.context.exceptions.ConfigurationException
import io.micronaut.core.io.ResourceLoader
import io.micronaut.discovery.config.RetryablePropertySourceImporter
import io.micronaut.retry.RetryPolicy
import spock.lang.Specification

class AzureKeyVaultPropertySourceImporterCoverageSpec extends Specification {

    void "importPropertySource uses cached client and materializes secrets"() {
        given:
        def importer = new AzureKeyVaultPropertySourceImporter()
        def settings = new AzureKeyVaultImportSettings('https://contoso.vault.azure.net', 'default', null, null, null, null, null, null, null, null)
        Map cache = importer.@clients
        cache.put(cacheKey(importer, settings), new StubSecretKeyVaultClient([
                new KeyVaultSecret('alpha-beta', 'value-1'),
                new KeyVaultSecret('gamma.delta', 'value-2')
        ]))
        def context = new StubImportContext(
                Stub(Environment),
                new RetryablePropertySourceImporter.RetryableImportDeclaration<>(settings, RetryPolicy.builder().build()),
                io.micronaut.core.util.ConnectionString.parse('azure-key-vault://contoso')
        )

        when:
        def propertySource = importer.importPropertySource(context).orElseThrow()

        then:
        propertySource.name == 'azure-key-vault://contoso'
        propertySource.get('alpha-beta') == 'value-1'
        propertySource.get('alpha.beta') == 'value-1'
        propertySource.get('gamma.delta') == 'value-2'
    }

    void "tokenCredential returns supported credential implementations"() {
        given:
        def importer = new AzureKeyVaultPropertySourceImporter()

        expect:
        expectedType.isInstance(invoke(importer, 'tokenCredential', settings))

        where:
        settings                                                                                                                                                 || expectedType
        new AzureKeyVaultImportSettings('https://contoso.vault.azure.net', 'default', null, null, null, null, null, null, null, null)                       || DefaultAzureCredential
        new AzureKeyVaultImportSettings('https://contoso.vault.azure.net', 'environment', null, null, null, null, null, null, null, null)                   || EnvironmentCredential
        new AzureKeyVaultImportSettings('https://contoso.vault.azure.net', 'cli', null, null, null, null, null, null, null, null)                           || AzureCliCredential
        new AzureKeyVaultImportSettings('https://contoso.vault.azure.net', 'managed-identity', null, null, null, null, null, null, null, null)              || ManagedIdentityCredential
        new AzureKeyVaultImportSettings('https://contoso.vault.azure.net', 'intellij', null, 'tenant', null, null, null, null, null, null)                  || IntelliJCredential
        new AzureKeyVaultImportSettings('https://contoso.vault.azure.net', 'visual-studio-code', null, 'tenant', null, null, null, null, null, null)        || VisualStudioCodeCredential
        new AzureKeyVaultImportSettings('https://contoso.vault.azure.net', 'client-secret', 'client', 'tenant', 'secret', null, null, null, null, null)     || ClientSecretCredential
        new AzureKeyVaultImportSettings('https://contoso.vault.azure.net', 'username-password', 'client', 'tenant', null, 'alice', 'secret', null, null, null) || UsernamePasswordCredential
    }

    void "clientCertificateCredential supports pem and pfx certificates"() {
        given:
        def importer = new AzureKeyVaultPropertySourceImporter()

        expect:
        invoke(importer, 'clientCertificateCredential', settings) instanceof ClientCertificateCredential

        where:
        settings << [
                new AzureKeyVaultImportSettings('https://contoso.vault.azure.net', 'client-certificate', 'client', 'tenant', null, null, null, '/tmp/client.pem', null, null),
                new AzureKeyVaultImportSettings('https://contoso.vault.azure.net', 'client-certificate', 'client', 'tenant', null, null, null, '/tmp/client.pfx', 'changeit', null)
        ]
    }

    void "tokenCredential rejects unsupported mode"() {
        given:
        def importer = new AzureKeyVaultPropertySourceImporter()

        when:
        invoke(importer, 'tokenCredential', new AzureKeyVaultImportSettings('https://contoso.vault.azure.net', 'bogus', null, null, null, null, null, null, null, null))

        then:
        def e = thrown(ConfigurationException)
        e.message.contains('Unsupported Azure Key Vault credential-mode')
    }

    void "newImportDeclaration rejects unsupported protocol"() {
        given:
        def importer = new AzureKeyVaultPropertySourceImporter()

        when:
        importer.newImportDeclaration(io.micronaut.core.util.ConnectionString.parse('consul://config'))

        then:
        def e = thrown(IllegalArgumentException)
        e.message.contains('Unsupported Azure Key Vault import protocol')
    }

    void "credential helper methods validate required values"() {
        given:
        def importer = new AzureKeyVaultPropertySourceImporter()

        when:
        invoke(importer, methodName, settings)

        then:
        def e = thrown(ConfigurationException)
        e.message.contains(expectedMessage)

        where:
        methodName                    | settings                                                                                                                                 || expectedMessage
        'clientSecretCredential'      | new AzureKeyVaultImportSettings('https://contoso.vault.azure.net', 'client-secret', null, 'tenant', 'secret', null, null, null, null, null) || 'credential-mode=client-secret'
        'clientCertificateCredential' | new AzureKeyVaultImportSettings('https://contoso.vault.azure.net', 'client-certificate', 'client', null, null, null, null, null, null, null) || 'credential-mode=client-certificate'
        'usernamePasswordCredential'  | new AzureKeyVaultImportSettings('https://contoso.vault.azure.net', 'username-password', 'client', null, null, null, null, null, null, null) || 'credential-mode=username-password'
    }

    void "merge returns declaration when it already has a vault url and resolves from environment otherwise"() {
        given:
        def importer = new AzureKeyVaultPropertySourceImporter()
        def declaration = new AzureKeyVaultImportSettings('https://declared.vault.azure.net', 'default', null, null, null, null, null, null, null, null)
        def unresolved = new AzureKeyVaultImportSettings(null, 'default', null, null, null, null, null, null, null, null)
        def environment = Stub(Environment) {
            getProperty('azure.key-vault.vault-url', String) >> Optional.of('https://resolved.vault.azure.net')
            getProperty('azure.key-vault.vaultUrl', String) >> Optional.empty()
        }

        expect:
        invoke(importer, 'merge', declaration, environment).is(declaration)
        invoke(importer, 'merge', unresolved, environment).vaultUrl() == 'https://resolved.vault.azure.net'
    }

    void "cache key ignores secret-bearing values"() {
        given:
        def importer = new AzureKeyVaultPropertySourceImporter()
        def left = new AzureKeyVaultImportSettings('https://contoso.vault.azure.net', 'client-secret', 'client', 'tenant', 'secret-1', 'alice', 'password-1', '/tmp/client.pfx', 'cert-1', 'managed-client')
        def right = new AzureKeyVaultImportSettings('https://contoso.vault.azure.net', 'client-secret', 'client', 'tenant', 'secret-2', 'alice', 'password-2', '/tmp/client.pfx', 'cert-2', 'managed-client')

        expect:
        cacheKey(importer, left) == cacheKey(importer, right)
    }

    private static Object invoke(Object target, String methodName, Object... args) {
        def method = target.class.declaredMethods.find { candidate ->
            candidate.name == methodName && candidate.parameterCount == args.length
        }
        method.accessible = true
        try {
            return method.invoke(target, args)
        } catch (java.lang.reflect.InvocationTargetException e) {
            throw e.targetException
        }
    }

    private static Object cacheKey(AzureKeyVaultPropertySourceImporter importer, AzureKeyVaultImportSettings settings) {
        def cacheKeyType = importer.class.declaredClasses.find { it.simpleName == 'CacheKey' }
        def method = cacheKeyType.getDeclaredMethod('of', AzureKeyVaultImportSettings)
        method.accessible = true
        method.invoke(null, settings)
    }

    private static final class StubSecretKeyVaultClient extends DefaultSecretKeyVaultClient {
        private final List<KeyVaultSecret> secrets

        StubSecretKeyVaultClient(List<KeyVaultSecret> secrets) {
            super(null)
            this.secrets = secrets
        }

        @Override
        List<KeyVaultSecret> listSecrets() {
            secrets
        }
    }

    private static final class StubImportContext implements PropertySourceImporter.ImportContext<RetryablePropertySourceImporter.RetryableImportDeclaration<AzureKeyVaultImportSettings>> {
        private final Environment environment
        private final RetryablePropertySourceImporter.RetryableImportDeclaration<AzureKeyVaultImportSettings> declaration
        private final io.micronaut.core.util.ConnectionString connectionString

        StubImportContext(Environment environment,
                          RetryablePropertySourceImporter.RetryableImportDeclaration<AzureKeyVaultImportSettings> declaration,
                          io.micronaut.core.util.ConnectionString connectionString) {
            this.environment = environment
            this.declaration = declaration
            this.connectionString = connectionString
        }

        @Override
        Environment environment() {
            environment
        }

        @Override
        io.micronaut.core.util.ConnectionString connectionString() {
            connectionString
        }

        @Override
        RetryablePropertySourceImporter.RetryableImportDeclaration<AzureKeyVaultImportSettings> importDeclaration() {
            declaration
        }

        @Override
        PropertySource.Origin parentOrigin() {
            null
        }

        @Override
        Optional<PropertySource> importPropertySource(ResourceLoader resourceLoader, String resourcePath, String sourceName, PropertySource.Origin origin) {
            Optional.empty()
        }

        @Override
        Optional<PropertySource> importPropertySource(String content, String sourceName, String extension, PropertySource.Origin origin) {
            Optional.empty()
        }

        @Override
        Optional<PropertySource> importClasspathPropertySource(String resourcePath, String sourceName, PropertySource.Origin origin, boolean allowMultiple) {
            Optional.empty()
        }
    }
}
