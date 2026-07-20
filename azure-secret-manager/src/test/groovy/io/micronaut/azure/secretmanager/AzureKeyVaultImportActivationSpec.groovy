package io.micronaut.azure.secretmanager

import spock.lang.Specification

class AzureKeyVaultImportActivationSpec extends Specification {

    void "import declaration is independent of config-client enabled flag"() {
        given:
        def importer = new AzureKeyVaultPropertySourceImporter()

        when:
        def declaration = importer.newImportDeclaration(io.micronaut.core.util.ConnectionString.parse('azure-key-vault://contoso-vault2'))

        then:
        declaration != null
        declaration.declaration().vaultUrl() == 'https://contoso-vault2.vault.azure.net'
    }
}
