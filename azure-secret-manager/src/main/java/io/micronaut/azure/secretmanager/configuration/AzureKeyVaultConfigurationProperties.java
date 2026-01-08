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
 * @author Nemanja Mikic
 */
@ConfigurationProperties(AzureKeyVaultConfigurationProperties.PREFIX)
@BootstrapContextCompatible
public class AzureKeyVaultConfigurationProperties {
    public static final String PREFIX = Environment.AZURE + ".key-vault";

    private String vaultURL;
    private KeysConfiguration keys = new KeysConfiguration();

    /**
     * @return key vault url.
     */
    public String getVaultURL() {
        return vaultURL;
    }

    /**
     * @param vaultURL key vault url.
     */
    public void setVaultURL(String vaultURL) {
        this.vaultURL = vaultURL;
    }

    /**
     * @return key specific configuration.
     */
    public KeysConfiguration getKeys() {
        return keys;
    }

    /**
     * @param keys key specific configuration.
     */
    public void setKeys(KeysConfiguration keys) {
        if (keys != null) {
            this.keys = keys;
        }
    }

    /**
     * Configuration for interacting with Key Vault keys.
     */
    @ConfigurationProperties("keys")
    @BootstrapContextCompatible
    public static class KeysConfiguration {
        public static final boolean DEFAULT_ENABLED = false;

        private boolean enabled = DEFAULT_ENABLED;
        private SigningConfiguration signing = new SigningConfiguration();

        /**
         * @return true if the key configuration client should be enabled.
         */
        public boolean isEnabled() {
            return enabled;
        }

        /**
         * @param enabled enable or disable the key configuration client.
         */
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        /**
         * @return signing configuration.
         */
        public SigningConfiguration getSigning() {
            return signing;
        }

        /**
         * @param signing signing configuration.
         */
        public void setSigning(SigningConfiguration signing) {
            if (signing != null) {
                this.signing = signing;
            }
        }

        /**
         * Configuration specific to signing operations.
         */
        @ConfigurationProperties("signing")
        @BootstrapContextCompatible
        public static class SigningConfiguration {

            private boolean enabled;
            private String defaultAlgorithm;

            /**
             * @return true if signing support is enabled.
             */
            public boolean isEnabled() {
                return enabled;
            }

            /**
             * @param enabled enable or disable signing support.
             */
            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            /**
             * @return the default signature algorithm (e.g. RS256).
             */
            public String getDefaultAlgorithm() {
                return defaultAlgorithm;
            }

            /**
             * @param defaultAlgorithm default algorithm (e.g. RS256).
             */
            public void setDefaultAlgorithm(String defaultAlgorithm) {
                this.defaultAlgorithm = defaultAlgorithm;
            }
        }
    }
}
