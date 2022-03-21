package io.micronaut.azure.secretmanager

import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.PropertySource
import reactor.core.publisher.Flux
import spock.lang.Requires
import spock.lang.Shared
import spock.lang.Specification

/**
 * This spec prerequisite is to have existing keyvault with one secret configured..
 */
@Requires({ System.getenv("AZURE_CLIENT_ID") && System.getenv("AZURE_CLIENT_SECRET") && System.getenv("AZURE_TENANT_ID") && System.getenv("AZURE_VAULT_URL") && System.getenv("VAULT_SECRET_NAME") && System.getenv("VAULT_SECRET_VALUE") })
class AzureSecretVaultConfigurationClientSpec extends Specification {

    @Shared
    String secretName = System.getenv("VAULT_SECRET_NAME")

    @Shared
    String secretValue = System.getenv("VAULT_SECRET_VALUE")

    void "it loads secret from vault"() {
        given:
        ApplicationContext ctx = ApplicationContext.run([
                "azure.keyvault.vaultUrl"        : System.getenv("AZURE_VAULT_URL"),
                'micronaut.config-client.enabled': true
        ])
        def client = ctx.getBean(AzureVaultConfigurationClient.class)

        when:
        PropertySource propertySource = Flux.from(client.getPropertySources(null)).blockFirst()

        then:
        !propertySource.isEmpty()
        propertySource.get(secretName) == secretValue
        propertySource.get(secretName.replace('-', '.')) == secretValue
        propertySource.get(secretName.replace('-', '_')) == secretValue
        propertySource.get('notFound') == null

        cleanup:
        ctx.close()
    }

}


