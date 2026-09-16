from typing import Annotated

from com.azure.storage.blob import BlobServiceClient
from jakarta.inject import Inject
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test


# AZURE_BLOB_ENDPOINT is set by the Gradle build for the tests
@MicronautTest
class BlobServiceFactoryTest:
    blob_service_client: Annotated[BlobServiceClient, Inject]

    @Test
    def test_creates_the_blob_service_client(self) -> None:
        assert self.blob_service_client.getAccountUrl() == "https://example.blob.core.windows.net"
