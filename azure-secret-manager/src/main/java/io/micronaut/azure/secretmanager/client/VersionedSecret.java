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

/**
 * A wrapper class around {@link com.azure.security.keyvault.secrets.models.KeyVaultSecret } with secret information.
 */
public class VersionedSecret {

    private final String name;
    private final String value;
    private final String version;

    public VersionedSecret(String name, String value, String version) {
        this.name = name;
        this.value = value;
        this.version = version;
    }

    /**
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return value
     */
    public String getValue() {
        return value;
    }

    /**
     *
     * @return version
     */
    public String getVersion() {
        return version;
    }
}
