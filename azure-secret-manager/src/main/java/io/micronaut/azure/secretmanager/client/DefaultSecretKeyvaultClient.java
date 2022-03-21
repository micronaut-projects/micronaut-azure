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
package io.micronaut.azure.secretmanager.client;


import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link SecretKeyvaultClient}.
 */
@Singleton
@BootstrapContextCompatible
@Requires(classes = SecretClient.class)
public class DefaultSecretKeyvaultClient implements SecretKeyvaultClient {

    private final SecretClient client;

    public DefaultSecretKeyvaultClient(SecretClient client) {
        this.client = client;
    }

    @Override
    public VersionedSecret getSecret(String secretName) {
        KeyVaultSecret secret = client.getSecret(secretName);

        return new VersionedSecret(secret.getName(), secret.getValue(), secret.getProperties().getVersion());
    }

    @Override
    public List<VersionedSecret> listSecrets() {
        return client.listPropertiesOfSecrets().stream().map(x -> getSecret(x.getName())).collect(Collectors.toList());
    }
}
