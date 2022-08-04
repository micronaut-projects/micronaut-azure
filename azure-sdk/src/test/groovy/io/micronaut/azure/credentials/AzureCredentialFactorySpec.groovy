package io.micronaut.azure.credentials

import com.azure.core.credential.TokenCredential
import com.azure.identity.AzureCliCredentialBuilder
import com.azure.identity.ClientSecretCredentialBuilder
import com.azure.identity.DefaultAzureCredential
import com.azure.identity.IntelliJCredentialBuilder
import com.azure.identity.ManagedIdentityCredentialBuilder
import com.azure.identity.UsernamePasswordCredentialBuilder
import com.azure.identity.VisualStudioCodeCredentialBuilder
import com.azure.storage.common.StorageSharedKeyCredential
import io.micronaut.context.ApplicationContext
import io.micronaut.context.exceptions.NoSuchBeanException
import spock.lang.Specification
import spock.lang.Unroll

class AzureCredentialFactorySpec extends Specification {

    def "it resolves client secret credential builder"() {
        given:
        def applicationContext = ApplicationContext.run([
                "azure.credential.client-secret.client-id": "client-id",
                "azure.credential.client-secret.tenant-id": "tenant-id",
                "azure.credential.client-secret.secret"   : "secret",
        ])

        expect:
        applicationContext.getBean(ClientSecretCredentialBuilder)

        cleanup:
        applicationContext.close()
    }

    def "it does not resolves client secret credential builder if client secret is missing"() {
        given:
        def applicationContext = ApplicationContext.run([
                "azure.credential.client-secret.client-id": "client-id",
                "azure.credential.client-secret.tenant-id": "tenant-id",
        ])

        when:
        applicationContext.getBean(ClientSecretCredentialBuilder)

        then:
        thrown(NoSuchBeanException)

        cleanup:
        applicationContext.close()
    }

    def "it resolves username password credential builder"() {
        given:
        def applicationContext = ApplicationContext.run([
                "azure.credential.username-password.client-id": "client-id",
                "azure.credential.username-password.tenant-id": "tenant-id",
                "azure.credential.username-password.username" : "username",
                "azure.credential.username-password.password" : "password",
        ])

        expect:
        applicationContext.getBean(UsernamePasswordCredentialBuilder)

        cleanup:
        applicationContext.close()
    }

    def "it fails to resolve username password credential builder if username is missing"() {
        given:
        def applicationContext = ApplicationContext.run([
                "azure.credential.username-password.client-id": "client-id",
                "azure.credential.username-password.tenant-id": "tenant-id",
                "azure.credential.username-password.password" : "password",
        ])

        when:
        applicationContext.getBean(UsernamePasswordCredentialBuilder)

        then:
        thrown(NoSuchBeanException)

        cleanup:
        applicationContext.close()
    }

    def "it fails to resolve username password credential builder if passsword is missing"() {
        given:
        def applicationContext = ApplicationContext.run([
                "azure.credential.username-password.client-id": "client-id",
                "azure.credential.username-password.tenant-id": "tenant-id",
                "azure.credential.username-password.username" : "password",
        ])

        when:
        applicationContext.getBean(UsernamePasswordCredentialBuilder)

        then:
        thrown(NoSuchBeanException)

        cleanup:
        applicationContext.close()
    }

    def "it fails to resolve username password credential builder if username and passsword is missing"() {
        given:
        def applicationContext = ApplicationContext.run([
                "azure.credential.username-password.client-id": "client-id",
                "azure.credential.username-password.tenant-id": "tenant-id",
        ])

        when:
        applicationContext.getBean(UsernamePasswordCredentialBuilder)

        then:
        thrown(NoSuchBeanException)

        cleanup:
        applicationContext.close()
    }

    @Unroll
    def "it resolves #enabled managed identity credential builder"(boolean enabled) {
        given:
        def applicationContext = ApplicationContext.run([
                "azure.credential.managed-identity.enabled"  : enabled,
                "azure.credential.managed-identity.client-id": "client-id"
        ])

        expect:
        applicationContext.containsBean(ManagedIdentityCredentialBuilder) == enabled

        cleanup:
        applicationContext.close()

        where:
        enabled << [true, false]
    }

    @Unroll
    def "it resolves #enabled azure cli credential builder"(boolean enabled) {
        given:
        def applicationContext = ApplicationContext.run([
                "azure.credential.cli.enabled": enabled,
        ])

        expect:
        applicationContext.containsBean(AzureCliCredentialBuilder) == enabled

        cleanup:
        applicationContext.close()

        where:
        enabled << [true, false]
    }

    @Unroll
    def "it resolves #enabled intellij credential builder"(boolean enabled) {
        given:
        def applicationContext = ApplicationContext.run([
                "azure.credential.intellij.enabled"               : enabled,
                "azure.credential.intellij.tenant-id"             : "tenant-id",
                "azure.credential.intellij.kee-pass-database-path": "keepasspath"
        ])

        expect:
        applicationContext.containsBean(IntelliJCredentialBuilder) == enabled

        cleanup:
        applicationContext.close()

        where:
        enabled << [true, false]
    }

    @Unroll
    def "it resolves #enabled visual studio code credential builder"(boolean enabled) {
        given:
        def applicationContext = ApplicationContext.run([
                "azure.credential.visual-studio-code.enabled": enabled,
        ])

        expect:
        applicationContext.containsBean(VisualStudioCodeCredentialBuilder) == enabled

        cleanup:
        applicationContext.close()

        where:
        enabled << [true, false]
    }

    def "it resolves default azure credentials if no other credentials are specified"() {
        given:
        def applicationContext = ApplicationContext.run([:])

        when:
        def tokenCredential = applicationContext.getBean(TokenCredential)

        then:
        noExceptionThrown()
        tokenCredential
        tokenCredential instanceof DefaultAzureCredential

        cleanup:
        applicationContext.close()
    }

    def "it resolves storage shared key credential configuration with connection string"() {
        given:
        def applicationContext = ApplicationContext.run([
                "azure.credential.storage-shared-key.connection-string": "DefaultEndpointsProtocol=https;AccountName=devstoreaccount1;AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;BlobEndpoint=https://127.0.0.1:10000/devstoreaccount1;"
        ])

        expect:
        applicationContext.getBean(StorageSharedKeyCredential)
    }

    def "it resolves storage shared key credential configuration with account name and key"() {
        given:
        def applicationContext = ApplicationContext.run([
                "azure.credential.storage-shared-key.account-name"  : "devstoreaccount1",
                "azure.credential.storage-shared-key.account-key"   : "Eby8vdM02xNOcqFlqUw..."
        ])

        expect:
        applicationContext.getBean(StorageSharedKeyCredential)
    }
}
