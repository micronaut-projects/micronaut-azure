/*
 * Copyright 2021 original authors
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
package io.micronaut.azure.condition;

import io.micronaut.azure.credentials.AzureCredentialsConfiguration;
import io.micronaut.context.condition.Condition;
import io.micronaut.context.condition.ConditionContext;

/**
 * The condition for the client certificate credential that evaluates true if there is one certificate path provided. In
 * case both certificate paths are provided the condition fails.
 *
 * @author Pavol Gressa
 * @since 3.1
 */
public class ClientCertificateCredentialsCondition implements Condition {
    final String BOTH_PATHS_ERROR = "Failed to create the client certificate credentials because both credential file paths (PEM and PFX) are provided in configuration.";

    @Override
    public boolean matches(ConditionContext context) {
        AzureCredentialsConfiguration.ClientCertificateCredentialConfiguration configuration =
                context.getBean(AzureCredentialsConfiguration.ClientCertificateCredentialConfiguration.class);
        if (configuration.getPemCertificatePath().isPresent() && configuration.getPfxCertificatePath().isPresent()) {
            context.fail(BOTH_PATHS_ERROR);
            return false;
        }
        return configuration.getPemCertificatePath().isPresent() || configuration.getPfxCertificatePath().isPresent();
    }
}
