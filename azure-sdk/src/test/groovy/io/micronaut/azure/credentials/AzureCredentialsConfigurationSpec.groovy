package io.micronaut.azure.credentials


import io.micronaut.context.ApplicationContext
import spock.lang.Specification

class AzureCredentialsConfigurationSpec extends Specification {

    def "it resolves client certificate credential configuration"() {
        given:
        def applicationContext = ApplicationContext.run([
                "azure.credentials.client-certificate.client-id"               : "client-id",
                "azure.credentials.client-certificate.pem-certificate-path"    : "pem",
                "azure.credentials.client-certificate.pfx-certificate-path"    : "pfx",
                "azure.credentials.client-certificate.pfx-certificate-password": "password",
                "azure.credentials.client-certificate.tenant-id"               : "tenant-id",
        ])

        when:
        def bean = applicationContext.getBean(AzureCredentialsConfiguration.ClientCertificateCredentialConfiguration)

        then:
        bean
        bean.clientId == "client-id"
        bean.pemCertificatePath.orElse(null) == "pem"
        bean.pfxCertificatePassword == "password"
        bean.pfxCertificatePath.orElse(null) == "pfx"
        bean.tenantId == "tenant-id"
    }

    def "it resolves client secret credential configuration"() {
        given:
        def applicationContext = ApplicationContext.run([
                "azure.credentials.client-secret.client-id": "client-id",
                "azure.credentials.client-secret.tenant-id": "tenant-id",
                "azure.credentials.client-secret.secret"   : "secret",
        ])

        when:
        def bean = applicationContext.getBean(AzureCredentialsConfiguration.ClientSecretCredentialConfiguration)

        then:
        bean
        bean.clientId == "client-id"
        bean.tenantId == "tenant-id"
        bean.secret == "secret"
    }

    def "it resolves username password credential configuration"() {
        given:
        def applicationContext = ApplicationContext.run([
                "azure.credentials.username-password.client-id": "client-id",
                "azure.credentials.username-password.tenant-id": "tenant-id",
                "azure.credentials.username-password.username" : "username",
                "azure.credentials.username-password.password" : "password",
        ])

        when:
        def bean = applicationContext.getBean(AzureCredentialsConfiguration.UsernamePasswordCredentialConfiguration)

        then:
        bean
        bean.clientId == "client-id"
        bean.tenantId.orElse(null) == "tenant-id"
        bean.username == "username"
        bean.password == "password"
    }

    def "it resolves managed identity credential configuration"() {
        given:
        def applicationContext = ApplicationContext.run([
                "azure.credentials.managed-identity.enabled"  : true,
                "azure.credentials.managed-identity.client-id": "client-id"
        ])

        when:
        def bean = applicationContext.getBean(AzureCredentialsConfiguration.ManagedIdentityCredentialConfiguration)

        then:
        bean
        bean.isEnabled()
        bean.clientId.orElse(null) == "client-id"
    }

    def "it resolves azure cli credential configuration"() {
        given:
        def applicationContext = ApplicationContext.run([
                "azure.credentials.cli.enabled": true,
        ])

        when:
        def bean = applicationContext.getBean(AzureCredentialsConfiguration.AzureCliCredentialConfiguration)

        then:
        bean
        bean.isEnabled()
    }

    def "it resolves intellij credential configuration"() {
        given:
        def applicationContext = ApplicationContext.run([
                "azure.credentials.intellij.enabled"               : true,
                "azure.credentials.intellij.tenant-id"             : "tenant-id",
                "azure.credentials.intellij.kee-pass-database-path": "keepasspath"
        ])

        when:
        def bean = applicationContext.getBean(AzureCredentialsConfiguration.IntelliJCredentialConfiguration)

        then:
        bean
        bean.isEnabled()
        bean.keePassDatabasePath.orElse(null) == "keepasspath"
        bean.tenantId.orElse(null) == "tenant-id"
    }

    def "it resolves visual studio code credential configuration"() {
        given:
        def applicationContext = ApplicationContext.run([
                "azure.credentials.visual-studio-code.enabled"  : true,
                "azure.credentials.visual-studio-code.tenant-id": "tenant-id"
        ])

        when:
        def bean = applicationContext.getBean(AzureCredentialsConfiguration.VisualStudioCodeCredentialConfiguration)

        then:
        bean
        bean.isEnabled()
        bean.tenantId.orElse(null) == "tenant-id"
    }
}
