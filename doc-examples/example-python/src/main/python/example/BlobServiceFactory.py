from com.azure.core.credential import TokenCredential
from com.azure.storage.blob import BlobServiceClient, BlobServiceClientBuilder
from jakarta.inject import Singleton
from java.lang import System
from micronaut.context.annotation import Factory


# tag::class[]
@Factory
class BlobServiceFactory:  # <1>

    @Singleton
    def blob_service_client(self, token_credential: TokenCredential) -> BlobServiceClient:  # <2>
        return (BlobServiceClientBuilder()  # <3>
                .credential(token_credential)
                .endpoint(System.getenv("AZURE_BLOB_ENDPOINT"))
                .buildClient())
# end::class[]
