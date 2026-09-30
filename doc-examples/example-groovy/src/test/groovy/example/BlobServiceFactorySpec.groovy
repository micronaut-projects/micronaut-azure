package example

import com.azure.storage.blob.BlobServiceClient
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

// AZURE_BLOB_ENDPOINT is set by the Gradle build for the tests
@MicronautTest
class BlobServiceFactorySpec extends Specification {

    @Inject
    BlobServiceClient blobServiceClient

    void "creates the blob service client"() {
        expect:
        blobServiceClient.accountUrl == "https://example.blob.core.windows.net"
    }
}
