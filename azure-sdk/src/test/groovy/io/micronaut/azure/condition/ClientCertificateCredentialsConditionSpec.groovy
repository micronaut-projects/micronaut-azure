package io.micronaut.azure.condition

import com.azure.identity.ClientCertificateCredentialBuilder
import io.micronaut.context.ApplicationContext
import io.micronaut.context.exceptions.NoSuchBeanException
import spock.lang.Specification

class ClientCertificateCredentialsConditionSpec extends Specification {

    def "it fails when both certificate paths are provided"() {
        given:
        def applicationContext = ApplicationContext.run([
                "azure.credential.client-certificate.client-id"                    : "client-id",
                "azure.credential.client-certificate.pfx-certificate-path-password": "password",
                "azure.credential.client-certificate.tenant-id"                    : "tenant-id",
                "azure.credential.client-certificate.pem-certificate-path"         : "path",
                "azure.credential.client-certificate.pfx-certificate-path"         : "path"
        ])

        when:
        applicationContext.getBean(ClientCertificateCredentialBuilder)

        then:
        thrown(NoSuchBeanException)

        cleanup:
        applicationContext.close()
    }

    def "it fails when none certificate path is provided"() {
        given:
        def applicationContext = ApplicationContext.run([
                "azure.credential.client-certificate.client-id"                    : "client-id",
                "azure.credential.client-certificate.pfx-certificate-path-password": "password",
                "azure.credential.client-certificate.tenant-id"                    : "tenant-id",
        ])

        when:
        applicationContext.getBean(ClientCertificateCredentialBuilder)

        then:
        thrown(NoSuchBeanException)

        cleanup:
        applicationContext.close()
    }

    def "it succeeds if pem certificate path is provided"() {
        given:
        def applicationContext = ApplicationContext.run([
                "azure.credential.client-certificate.client-id"                    : "client-id",
                "azure.credential.client-certificate.pfx-certificate-path-password": "password",
                "azure.credential.client-certificate.tenant-id"                    : "tenant-id",
                "azure.credential.client-certificate.pem-certificate-path"         : "path",
        ])

        when:
        applicationContext.getBean(ClientCertificateCredentialBuilder)

        then:
        noExceptionThrown()

        cleanup:
        applicationContext.close()
    }

    def "it succeeds if pfx certificate path is provided"() {
        given:
        def applicationContext = ApplicationContext.run([
                "azure.credential.client-certificate.client-id"                    : "client-id",
                "azure.credential.client-certificate.pfx-certificate-path-password": "password",
                "azure.credential.client-certificate.tenant-id"                    : "tenant-id",
                "azure.credential.client-certificate.pfx-certificate-path"         : "path"
        ])

        when:
        applicationContext.getBean(ClientCertificateCredentialBuilder)

        then:
        noExceptionThrown()

        cleanup:
        applicationContext.close()
    }
}
