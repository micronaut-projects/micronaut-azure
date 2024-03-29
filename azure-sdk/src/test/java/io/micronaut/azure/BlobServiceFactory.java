/*
 * Copyright 2021 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.azure;

import com.azure.core.credential.TokenCredential;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Singleton;

@Requires(property = "spec.name", value = "AzureClientFactorySpec")
//tag::class[]
@Factory
public class BlobServiceFactory {  // <1>

    @Singleton
    public BlobServiceClient blobServiceClient(@NonNull TokenCredential tokenCredential){  // <2>
        return new BlobServiceClientBuilder()  // <3>
                .credential(tokenCredential)
                .endpoint(System.getenv("AZURE_BLOB_ENDPOINT"))
                .buildClient();
    }
}
//end::class[]
