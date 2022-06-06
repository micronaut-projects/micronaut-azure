package io.micronaut.azure.secretmanager

import io.micronaut.azure.secretmanager.configuration.AzureKeyVaultConfigurationProperties
import io.micronaut.context.ApplicationContext
import io.micronaut.context.exceptions.NoSuchBeanException
import spock.lang.Specification

class AzureSecretVaultConfigurationSpec extends Specification {

    void "it parses configuration"() {
        ApplicationContext ctx = ApplicationContext.run([
                'spec.name'                      : 'it parses configuration',
                "azure.key-vault.vaultUrl"       : "https://www.azure.com",
                'micronaut.config-client.enabled': false

        ])
        AzureKeyVaultConfigurationProperties config = ctx.getBean(AzureKeyVaultConfigurationProperties)

        expect:
        "https://www.azure.com" == config.vaultURL

        cleanup:
        ctx.close()
    }

    void "it is missing vault configuration client bean when disabled"() {
        given:
        ApplicationContext ctx = ApplicationContext.run()

        when:
        ctx.getBean(AzureVaultConfigurationClient)

        then:
        thrown NoSuchBeanException

        cleanup:
        ctx.close()
    }

}
