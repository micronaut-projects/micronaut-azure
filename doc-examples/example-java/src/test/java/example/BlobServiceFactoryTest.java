package example;

import com.azure.storage.blob.BlobServiceClient;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

// AZURE_BLOB_ENDPOINT is set by the Gradle build for the tests
@MicronautTest
class BlobServiceFactoryTest {

    @Inject
    BlobServiceClient blobServiceClient;

    @Test
    void createsTheBlobServiceClient() {
        assertEquals("https://example.blob.core.windows.net", blobServiceClient.getAccountUrl());
    }
}
