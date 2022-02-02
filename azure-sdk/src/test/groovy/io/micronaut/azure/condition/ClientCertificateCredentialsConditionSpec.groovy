package io.micronaut.azure.condition

import com.azure.identity.ClientCertificateCredentialBuilder
import io.micronaut.context.ApplicationContext
import io.micronaut.context.exceptions.NoSuchBeanException
import spock.lang.Specification

class ClientCertificateCredentialsConditionSpec extends Specification {

    def "it fails when both certificate paths are provided"() {
        given:
        def applicationContext = ApplicationContext.run([
                "azure.credentials.client-certificate.pem-certificate-path": "path",
                "azure.credentials.client-certificate.pfx-certificate-path": "path"
        ])

        when:
        applicationContext.getBean(ClientCertificateCredentialBuilder)

        then:
        thrown(NoSuchBeanException)
    }

    def "it fails when none certificate path is provided"() {
        given:
        def applicationContext = ApplicationContext.run([:])

        when:
        applicationContext.getBean(ClientCertificateCredentialBuilder)

        then:
        thrown(NoSuchBeanException)

    }

    def "it succeeds if pem certificate path is provided"() {
        given:
        def applicationContext = ApplicationContext.run([
                "azure.credentials.client-certificate.pem-certificate-path": "path",
        ])

        when:
        def bean = applicationContext.getBean(ClientCertificateCredentialBuilder)

        then:
        bean

    }

    def "it succeeds if pfx certificate path is provided"() {
        given:
        def applicationContext = ApplicationContext.run([
                "azure.credentials.client-certificate.pfx-certificate-path": "path"
        ])

        when:
        def bean = applicationContext.getBean(ClientCertificateCredentialBuilder)

        then:
        bean

    }
}
