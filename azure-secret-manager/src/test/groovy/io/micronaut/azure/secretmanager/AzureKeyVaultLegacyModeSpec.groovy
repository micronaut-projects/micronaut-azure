package io.micronaut.azure.secretmanager

import io.micronaut.context.env.Environment
import spock.lang.Specification

class AzureKeyVaultLegacyModeSpec extends Specification {

    void "it detects string config import declarations"() {
        expect:
        AzureKeyVaultLegacyMode.isImportConfigured(environmentWith('azure-key-vault://example-vault'))
    }

    void "it detects optional string config import declarations"() {
        expect:
        AzureKeyVaultLegacyMode.isImportConfigured(environmentWith('optional:azure-key-vault://example-vault'))
    }

    void "it detects list config import declarations"() {
        expect:
        AzureKeyVaultLegacyMode.isImportConfigured(environmentWith(['file://application-test.yml', 'azure-key-vault://example-vault']))
    }

    void "it detects provider map config import declarations"() {
        expect:
        AzureKeyVaultLegacyMode.isImportConfigured(environmentWith([provider: 'azure-key-vault', name: 'example-vault']))
    }

    void "it ignores unrelated config import declarations"() {
        expect:
        !AzureKeyVaultLegacyMode.isImportConfigured(environmentWith(['file://application-test.yml', [provider: 'consul', serviceId: 'demo']]))
    }

    private Environment environmentWith(Object value) {
        Stub(Environment) {
            getProperty('micronaut.config.import', Object) >> Optional.of(value)
        }
    }
}
