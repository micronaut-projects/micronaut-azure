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

import io.micronaut.context.env.Environment;
import io.micronaut.core.annotation.Internal;

import java.util.Map;

@Internal
final class AzureKeyVaultLegacyMode {

    private static final String CONFIG_IMPORT = "micronaut.config.import";
    private static final String IMPORT_PREFIX = "azure-key-vault://";
    private static final String OPTIONAL_IMPORT_PREFIX = "optional:" + IMPORT_PREFIX;
    private static final String PROVIDER = "provider";
    private static final String AZURE_KEY_VAULT = "azure-key-vault";

    private AzureKeyVaultLegacyMode() {
    }

    static boolean isImportConfigured(Environment environment) {
        return environment.getProperty(CONFIG_IMPORT, Object.class)
                .map(AzureKeyVaultLegacyMode::containsAzureKeyVaultImport)
                .orElse(false);
    }

    private static boolean containsAzureKeyVaultImport(Object value) {
        if (value instanceof CharSequence sequence) {
            return containsAzureKeyVaultImport(sequence.toString());
        }
        if (value instanceof Iterable<?> iterable) {
            for (Object item : iterable) {
                if (containsAzureKeyVaultImport(item)) {
                    return true;
                }
            }
        }
        if (value instanceof Map<?, ?> map) {
            Object provider = map.get(PROVIDER);
            if (provider instanceof CharSequence sequence) {
                return AZURE_KEY_VAULT.equals(sequence.toString());
            }
        }
        return false;
    }

    private static boolean containsAzureKeyVaultImport(String value) {
        return value.contains(IMPORT_PREFIX) || value.contains(OPTIONAL_IMPORT_PREFIX);
    }
}
