/*
 * Copyright 2017-2022 original authors
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
package io.micronaut.azure.cosmos.client;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosItemSerializer;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Nullable;
import jakarta.inject.Singleton;

/**
 * The Azure Cosmos Client factory.
 *
 * @author radovanradic
 * @since 3.5.0
 */
@Factory
@Internal
final class CosmosClientFactory {

    private final @Nullable CosmosItemSerializer customCosmosItemSerializer;

    CosmosClientFactory(@Nullable CosmosItemSerializer customCosmosItemSerializer) {
        this.customCosmosItemSerializer = customCosmosItemSerializer;
    }

    /**
     * Creates sync Cosmos client.
     *
     * @param cosmosClientBuilder the Cosmos client builder
     * @return an instance of {@link CosmosClient}
     */
    @Bean(preDestroy = "close")
    @Singleton
    @Requires(beans = CosmosClientBuilder.class)
    CosmosClient buildCosmosClient(CosmosClientBuilder cosmosClientBuilder) {
        return cosmosClientBuilder.buildClient();
    }

    @Singleton
    @Requires(beans = CosmosClientConfiguration.class)
    CosmosClientBuilder createCosmosClientBuilder(CosmosClientConfiguration configuration) {
        CosmosClientBuilder cosmosClientBuilder = configuration.getCosmosClientBuilder();
        if (this.customCosmosItemSerializer != null) {
            cosmosClientBuilder = cosmosClientBuilder.customItemSerializer(this.customCosmosItemSerializer);
        }
        return cosmosClientBuilder;
    }

    /**
     * Creates async Cosmos client.
     *
     * @param cosmosClientBuilder the Cosmos client builder
     * @return an instance of {@link CosmosAsyncClient}
     */
    @Bean(preDestroy = "close")
    @Singleton
    @Requires(beans = CosmosClientBuilder.class)
    CosmosAsyncClient buildCosmosAsyncClient(CosmosClientBuilder cosmosClientBuilder) {
        return cosmosClientBuilder.buildAsyncClient();
    }
}
