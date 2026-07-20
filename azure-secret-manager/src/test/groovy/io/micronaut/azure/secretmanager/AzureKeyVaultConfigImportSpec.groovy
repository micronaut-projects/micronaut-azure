package io.micronaut.azure.secretmanager

import io.micronaut.context.ApplicationContext
import spock.lang.Requires
import spock.lang.Shared
import spock.lang.Specification

@Requires({
    System.getenv("AZURE_CLIENT_ID")
            && System.getenv("AZURE_CLIENT_SECRET")
            && System.getenv("AZURE_TENANT_ID")
            && System.getenv("AZURE_VAULT_URL")
            && System.getenv("VAULT_SECRET_NAME")
            && System.getenv("VAULT_SECRET_VALUE")
})
class AzureKeyVaultConfigImportSpec extends Specification {

    @Shared
    String secretName = System.getenv("VAULT_SECRET_NAME")

    @Shared
    String secretValue = System.getenv("VAULT_SECRET_VALUE")

    @Shared
    String vaultUrl = System.getenv("AZURE_VAULT_URL")

    @Shared
    String configImport = "azure-key-vault://${vaultName(vaultUrl)}?credential-mode=client-secret&client-id=${System.getenv('AZURE_CLIENT_ID')}&tenant-id=${System.getenv('AZURE_TENANT_ID')}&client-secret=${System.getenv('AZURE_CLIENT_SECRET')}"

    void "it loads secrets via micronaut config import"() {
        given:
        ApplicationContext context = ApplicationContext.run([
                'micronaut.config.import': configImport
        ])

        expect:
        context.getProperty(secretName, String).orElse(null) == secretValue
        context.getProperty(secretName.replace('-', '.'), String).orElse(null) == secretValue
        context.getProperty(secretName.replace('-', '_'), String).orElse(null) == secretValue
        context.getProperty('notFound', String).isEmpty()

        cleanup:
        context.close()
    }

    private static String vaultName(String vaultUrl) {
        URI uri = URI.create(vaultUrl)
        String host = uri.host ?: vaultUrl
        return host.replaceFirst(/\.vault\.azure\.net$/, '')
    }
}
