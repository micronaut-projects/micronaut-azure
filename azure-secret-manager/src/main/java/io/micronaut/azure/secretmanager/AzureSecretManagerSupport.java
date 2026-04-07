/*
 * Copyright 2017-2026 original authors
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
import io.micronaut.core.annotation.Internal;

/**
 * Internal support for creating Azure Secret Manager SDK clients from already-resolved
 * vault and credential settings shared by bootstrap and config-import code paths.
 */
@Internal
final class AzureSecretManagerSupport {

    private AzureSecretManagerSupport() {
    }

    static SecretClient secretClient(String vaultUrl, TokenCredential tokenCredential) {
        return new SecretClientBuilder()
                .vaultUrl(vaultUrl)
                .credential(tokenCredential)
                .buildClient();
    }
}
