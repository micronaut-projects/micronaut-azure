package example

import com.azure.core.credential.TokenCredential
import com.azure.storage.blob.BlobServiceClient
import com.azure.storage.blob.BlobServiceClientBuilder
import io.micronaut.context.annotation.Factory
import jakarta.inject.Singleton

//tag::class[]
@Factory
class BlobServiceFactory { // <1>

    @Singleton
    fun blobServiceClient(tokenCredential: TokenCredential): BlobServiceClient { // <2>
        return BlobServiceClientBuilder() // <3>
            .credential(tokenCredential)
            .endpoint(System.getenv("AZURE_BLOB_ENDPOINT"))
            .buildClient()
    }
}
//end::class[]
