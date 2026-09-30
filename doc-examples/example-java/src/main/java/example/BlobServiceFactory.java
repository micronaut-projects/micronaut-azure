package example;

import com.azure.core.credential.TokenCredential;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;
import org.jspecify.annotations.NonNull;

//tag::class[]
@Factory
public class BlobServiceFactory {  // <1>

    @Singleton
    public BlobServiceClient blobServiceClient(@NonNull TokenCredential tokenCredential) {  // <2>
        return new BlobServiceClientBuilder()  // <3>
                .credential(tokenCredential)
                .endpoint(System.getenv("AZURE_BLOB_ENDPOINT"))
                .buildClient();
    }
}
//end::class[]
