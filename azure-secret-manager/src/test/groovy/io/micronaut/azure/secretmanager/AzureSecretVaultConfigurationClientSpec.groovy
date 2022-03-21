package io.micronaut.azure.secretmanager

import com.azure.security.keyvault.secrets.SecretClient
import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.PropertySource
import reactor.core.publisher.Flux
import spock.lang.Requires
import spock.lang.Shared
import spock.lang.Specification

/**
 * This spec prerequisite is to have existing vault with one secret configured..
 */
@Requires({ System.getenv("AZURE_CLIENT_ID") && System.getenv("AZURE_CLIENT_SECRET") && System.getenv("AZURE_TENANT_ID") && System.getenv("AZURE_VAULT_URL") })
class AzureSecretVaultConfigurationClientSpec extends Specification {

    @Shared
    String secretName = "mc-test"

    @Shared
    String secretValue = "secretval"

    void "missing secret"() {
        ApplicationContext context = ApplicationContext.run([
                "azure.keyvault.vaultUrl"        : System.getenv("AZURE_VAULT_URL"),
                'micronaut.config-client.enabled': true
        ])
        def client = context.getBean(SecretClient)
        when:
        client.getSecret("notfound")
        then:
        thrown Exception
    }

    void "fetch single secret"() {
        ApplicationContext context = ApplicationContext.run([
                "azure.keyvault.vaultUrl"        : System.getenv("AZURE_VAULT_URL"),
                'micronaut.config-client.enabled': true
        ])
        def client = context.getBean(SecretClient)
        when:
        def result = client.getSecret("mc-test")
        then:
        result.getName() == "mc-test"
        result.getValue() != null
    }

    void "fetch single with version"() {
        ApplicationContext context = ApplicationContext.run([
                "azure.keyvault.vaultUrl"        : System.getenv("AZURE_VAULT_URL"),
                'micronaut.config-client.enabled': true
        ])
        def client = context.getBean(SecretClient)
        when:

        def result = client.getSecret("mc-test", "57f02ab04748473182ae0ca9dc72b393")
        then:
        result.getName() == "mc-test"
        result.getValue() != null
    }


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


