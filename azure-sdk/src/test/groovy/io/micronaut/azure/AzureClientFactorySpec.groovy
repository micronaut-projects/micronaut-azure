package io.micronaut.azure

import com.azure.storage.blob.BlobServiceClient
import io.micronaut.context.ApplicationContext
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest
class AzureClientFactorySpec extends Specification implements TestPropertyProvider {

    @Inject
    ApplicationContext applicationContext

    Map<String, String> getProperties() {
        return ["spec.name": "AzureClientFactorySpec"]
    }

    def "it creates the blob service client"() {
        when:
        def blobService = applicationContext.getBean(BlobServiceClient)

        then:
        blobService

        when:
        def containers = blobService.listBlobContainers().asList()

        then:
        noExceptionThrown()
    }
}
