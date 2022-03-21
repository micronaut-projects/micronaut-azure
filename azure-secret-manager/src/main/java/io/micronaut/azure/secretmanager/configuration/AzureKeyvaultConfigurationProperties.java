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
package io.micronaut.azure.secretmanager.configuration;

import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.env.Environment;


/**
 * Configuration for azure SecretClient.
 */
@ConfigurationProperties(AzureKeyvaultConfigurationProperties.PREFIX)
@BootstrapContextCompatible
public class AzureKeyvaultConfigurationProperties {
    public static final String PREFIX = Environment.AZURE + ".keyvault";

    private String vaultURL;

    /**
     * @return keyvault url.
     */
    public String getVaultURL() {
        return vaultURL;
    }

    /**
     * @param vaultURL keyvault url.
     */
    public void setVaultURL(String vaultURL) {
        this.vaultURL = vaultURL;
    }

}
