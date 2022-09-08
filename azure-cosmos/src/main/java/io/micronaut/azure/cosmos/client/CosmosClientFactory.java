/*
 * Copyright 2017-2020 original authors
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
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Internal;

/**
 * The Azure Cosmos Client factory.
 *
 * @author radovanradic
 * @since 3.5.0
 */
@Factory
@Internal
final class CosmosClientFactory {

    /**
     * Creates sync Cosmos client.
     *
     * @param configuration the Cosmos client configuration
     * @return an instance of {@link CosmosClient}
     */
    @Bean(preDestroy = "close")
    @Requires(beans = CosmosClientConfiguration.class)
    CosmosClient buildCosmosClient(CosmosClientConfiguration configuration) {
        return configuration.getCosmosClientBuilder().buildClient();
    }

    /**
     * Creates async Cosmos client.
     *
     * @param configuration the Cosmos client configuration
     * @return an instance of {@link CosmosAsyncClient}
     */
    @Bean(preDestroy = "close")
    @Requires(beans = CosmosClientConfiguration.class)
    CosmosAsyncClient buildCosmosAsyncClient(CosmosClientConfiguration configuration) {
        return configuration.getCosmosClientBuilder().buildAsyncClient();
    }
}
