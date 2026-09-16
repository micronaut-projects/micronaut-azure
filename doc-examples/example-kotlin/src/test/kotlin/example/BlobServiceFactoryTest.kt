package example

import com.azure.storage.blob.BlobServiceClient
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

// AZURE_BLOB_ENDPOINT is set by the Gradle build for the tests
@MicronautTest
class BlobServiceFactoryTest {

    @Inject
    lateinit var blobServiceClient: BlobServiceClient

    @Test
    fun createsTheBlobServiceClient() {
        assertEquals("https://example.blob.core.windows.net", blobServiceClient.accountUrl)
    }
}
