package io.micronaut.azure.credentials


import io.micronaut.context.ApplicationContext
import spock.lang.Specification

class AzureCredentialsConfigurationSpec extends Specification {

    def "it resolves client certificate credential configuration"() {
        given:
        def applicationContext = ApplicationContext.run([
                "azure.credential.client-certificate.client-id"               : "client-id",
                "azure.credential.client-certificate.pem-certificate-path"    : "pem",
                "azure.credential.client-certificate.pfx-certificate-path"    : "pfx",
                "azure.credential.client-certificate.pfx-certificate-password": "password",
                "azure.credential.client-certificate.tenant-id"               : "tenant-id",
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
                "azure.credential.client-secret.client-id": "client-id",
                "azure.credential.client-secret.tenant-id": "tenant-id",
                "azure.credential.client-secret.secret"   : "secret",
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
                "azure.credential.username-password.client-id": "client-id",
                "azure.credential.username-password.tenant-id": "tenant-id",
                "azure.credential.username-password.username" : "username",
                "azure.credential.username-password.password" : "password",
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
                "azure.credential.managed-identity.enabled"  : true,
                "azure.credential.managed-identity.client-id": "client-id"
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
                "azure.credential.cli.enabled": true,
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
                "azure.credential.intellij.enabled"               : true,
                "azure.credential.intellij.tenant-id"             : "tenant-id",
                "azure.credential.intellij.kee-pass-database-path": "keepasspath"
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
                "azure.credential.visual-studio-code.enabled"  : true,
                "azure.credential.visual-studio-code.tenant-id": "tenant-id"
        ])

        when:
        def bean = applicationContext.getBean(AzureCredentialsConfiguration.VisualStudioCodeCredentialConfiguration)

        then:
        bean
        bean.isEnabled()
        bean.tenantId.orElse(null) == "tenant-id"
    }

    def "it resolves storage shared key credential configuration with connection string"() {
        given:
        def applicationContext = ApplicationContext.run([
                "azure.credential.storage-shared-key.connection-string": "DefaultEndpointsProtocol=..."
        ])

        when:
        def bean = applicationContext.getBean(AzureCredentialsConfiguration.StorageSharedKeyCredentialConfiguration)

        then:
        bean
        bean.connectionString.orElse(null) == "DefaultEndpointsProtocol=..."
        !bean.accountName.isPresent()
        !bean.accountKey.isPresent()
    }

    def "it resolves storage shared key credential configuration with account name and key"() {
        given:
        def applicationContext = ApplicationContext.run([
                "azure.credential.storage-shared-key.account-name"  : "devstoreaccount1",
                "azure.credential.storage-shared-key.account-key"   : "Eby8vdM02xNOcqFlqUw..."
        ])

        when:
        def bean = applicationContext.getBean(AzureCredentialsConfiguration.StorageSharedKeyCredentialConfiguration)

        then:
        bean
        !bean.connectionString.isPresent()
        bean.accountName.orElse(null) == "devstoreaccount1"
        bean.accountKey.orElse(null) == "Eby8vdM02xNOcqFlqUw..."
    }
}
