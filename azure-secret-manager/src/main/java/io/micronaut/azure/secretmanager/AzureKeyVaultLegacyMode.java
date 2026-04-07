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

@Internal
final class AzureKeyVaultLegacyMode {

    private static final String CONFIG_IMPORT = "micronaut.config.import";
    private static final String IMPORT_PREFIX = "azure-key-vault://";

    private AzureKeyVaultLegacyMode() {
    }

    static boolean isImportConfigured(Environment environment) {
        return environment.getProperty(CONFIG_IMPORT, String.class)
                .map(value -> value.contains(IMPORT_PREFIX) || value.contains("optional:" + IMPORT_PREFIX))
                .orElse(false);
    }
}
