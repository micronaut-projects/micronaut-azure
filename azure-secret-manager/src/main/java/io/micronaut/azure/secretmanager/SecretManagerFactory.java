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
package io.micronaut.azure.secretmanager;

import com.azure.core.credential.TokenCredential;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import io.micronaut.azure.secretmanager.configuration.AzureKeyvaultConfigurationProperties;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.StringUtils;
import io.micronaut.discovery.config.ConfigurationClient;
import jakarta.inject.Singleton;


/**
 * Factory to create Azure Secret client.
 */
@Factory
@Requires(property = ConfigurationClient.ENABLED, value = StringUtils.TRUE, defaultValue = StringUtils.FALSE)
@Requires(property = AzureKeyvaultConfigurationProperties.PREFIX)
@BootstrapContextCompatible
public class SecretManagerFactory {

    /**
     * Creates a {@link SecretClient} instance.
     *
     * @param tokenCredential                      azure credentials
     * @param azureKeyvaultConfigurationProperties keyvault configuration
     * @return an instance using defaults.
     */
    @Singleton
    public SecretClient secretAsyncClient(
            @NonNull TokenCredential tokenCredential,
            @NonNull AzureKeyvaultConfigurationProperties azureKeyvaultConfigurationProperties
    ) {
        return new SecretClientBuilder()
                .vaultUrl(azureKeyvaultConfigurationProperties.getVaultURL())
                .credential(tokenCredential)
                .buildClient();
    }

}
