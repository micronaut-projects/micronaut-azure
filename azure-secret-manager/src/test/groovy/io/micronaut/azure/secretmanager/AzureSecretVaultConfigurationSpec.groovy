package io.micronaut.azure.secretmanager

import io.micronaut.azure.secretmanager.configuration.AzureKeyvaultConfigurationProperties
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Replaces
import io.micronaut.context.annotation.Requires
import io.micronaut.context.env.Environment
import io.micronaut.context.env.PropertySource
import io.micronaut.context.exceptions.NoSuchBeanException
import jakarta.inject.Singleton
import org.reactivestreams.Publisher
import reactor.core.publisher.Flux
import spock.lang.Specification

class AzureSecretVaultConfigurationSpec extends Specification {

    void "it parses configuration"() {
        ApplicationContext ctx = ApplicationContext.run([
                'spec.name'                               : 'it parses configuration',
                "azure.keyvault.vaultUrl"                 : "https://www.azure.com",
                'micronaut.config-client.enabled'         : true

        ])
        AzureKeyvaultConfigurationProperties config = ctx.getBean(AzureKeyvaultConfigurationProperties)

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

    @Singleton
    @Replaces(AzureVaultConfigurationClient)
    @Requires(property = 'spec.name', value = 'it parses configuration')
    static class MockOracleCloudVaultConfigurationClient extends AzureVaultConfigurationClient {

        MockOracleCloudVaultConfigurationClient() {
            super(null, null, null)
        }

        @Override
        Publisher<PropertySource> getPropertySources(Environment environment) {
            return Flux.empty()
        }
    }

}
