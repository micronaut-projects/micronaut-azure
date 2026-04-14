package io.micronaut.azure.secretmanager

import io.micronaut.context.exceptions.ConfigurationException
import io.micronaut.context.env.Environment
import io.micronaut.core.convert.value.ConvertibleValues
import io.micronaut.core.util.ConnectionString
import spock.lang.Specification

class AzureKeyVaultImportSettingsSpec extends Specification {

    void "from connection string defaults credential mode and expands vault name"() {
        when:
        def settings = AzureKeyVaultImportSettings.fromConnectionString(ConnectionString.parse('azure-key-vault://contoso-vault2'))

        then:
        settings.vaultUrl() == 'https://contoso-vault2.vault.azure.net'
        settings.credentialMode() == 'default'
    }

    void "from values defaults credential mode and expands name"() {
        when:
        def settings = AzureKeyVaultImportSettings.fromValues(ConvertibleValues.of([name: 'contoso-vault2']))

        then:
        settings.vaultUrl() == 'https://contoso-vault2.vault.azure.net'
        settings.credentialMode() == 'default'
    }

    void "from values prefers explicit vault url"() {
        when:
        def settings = AzureKeyVaultImportSettings.fromValues(ConvertibleValues.of([
                'vault-url'               : 'https://custom.vault.azure.net',
                'credential-mode'         : 'managed-identity',
                'managed-identity-client-id': 'managed-client'
        ]))

        then:
        settings.vaultUrl() == 'https://custom.vault.azure.net'
        settings.credentialMode() == 'managed-identity'
        settings.managedIdentityClientId() == 'managed-client'
    }

    void "resolve reads kebab case vault url"() {
        given:
        def environment = Stub(Environment) {
            getProperty('azure.key-vault.vault-url', String) >> Optional.of('https://kebab.vault.azure.net')
            getProperty('azure.key-vault.vaultUrl', String) >> Optional.empty()
        }

        when:
        def settings = AzureKeyVaultImportSettings.resolve(environment)

        then:
        settings.vaultUrl() == 'https://kebab.vault.azure.net'
        settings.credentialMode() == 'default'
    }

    void "resolve falls back to camel case vault url"() {
        given:
        def environment = Stub(Environment) {
            getProperty('azure.key-vault.vault-url', String) >> Optional.empty()
            getProperty('azure.key-vault.vaultUrl', String) >> Optional.of('https://camel.vault.azure.net')
        }

        when:
        def settings = AzureKeyVaultImportSettings.resolve(environment)

        then:
        settings.vaultUrl() == 'https://camel.vault.azure.net'
        settings.credentialMode() == 'default'
    }

    void "resolve rejects missing vault url"() {
        given:
        def environment = Stub(Environment) {
            getProperty('azure.key-vault.vault-url', String) >> Optional.empty()
            getProperty('azure.key-vault.vaultUrl', String) >> Optional.empty()
        }

        when:
        AzureKeyVaultImportSettings.resolve(environment)

        then:
        def e = thrown(ConfigurationException)
        e.message.contains('azure.key-vault.vault-url')
    }
}
