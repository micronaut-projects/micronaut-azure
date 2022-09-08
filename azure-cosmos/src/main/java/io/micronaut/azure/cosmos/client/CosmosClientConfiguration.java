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

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClientBuilder;
import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.env.Environment;
import io.micronaut.core.annotation.NonNull;

/**
 * The default Azure Cosmos configuration class.
 *
 * @author radovanradic
 * @since 3.5.0
 */
@ConfigurationProperties(CosmosClientConfiguration.PREFIX)
public final class CosmosClientConfiguration {
    static final String PREFIX = Environment.AZURE + ".cosmos";
    private static final ConsistencyLevel DEFAULT_CONSISTENCY_LEVEL = ConsistencyLevel.EVENTUAL;
    private static final boolean DEFAULT_ENDPOINT_DISCOVERY = true;

    @ConfigurationBuilder(prefixes = "")
    protected CosmosClientBuilder cosmosClientBuilder = new CosmosClientBuilder();

    private boolean defaultGatewayMode;

    private boolean endpointDiscoveryEnabled = DEFAULT_ENDPOINT_DISCOVERY;

    private ConsistencyLevel consistencyLevel = DEFAULT_CONSISTENCY_LEVEL;

    /**
     * @return the Cosmos Client Builder
     */
    @NonNull
    public CosmosClientBuilder getCosmosClientBuilder() {

        if (defaultGatewayMode) {
            cosmosClientBuilder.gatewayMode();
        }
        return cosmosClientBuilder
            .endpointDiscoveryEnabled(endpointDiscoveryEnabled)
            .consistencyLevel(consistencyLevel);
    }

    /**
     * When the value is true then default gateway configuration will be used in cosmos client.
     * @param defaultGatewayMode set the default gateway mode
     */
    public void setDefaultGatewayMode(boolean defaultGatewayMode) {
        this.defaultGatewayMode = defaultGatewayMode;
    }

    /**
     * @param endpointDiscoveryEnabled set the endpoint discovery flag
     */
    public void setEndpointDiscoveryEnabled(boolean endpointDiscoveryEnabled) {
        this.endpointDiscoveryEnabled = endpointDiscoveryEnabled;
    }

    /**
     * Gives ability to configure consistency level.
     *
     * @param consistencyLevel sets the default consistency level
     */
    public void setConsistencyLevel(@NonNull ConsistencyLevel consistencyLevel) {
        this.consistencyLevel = consistencyLevel;
    }
}
