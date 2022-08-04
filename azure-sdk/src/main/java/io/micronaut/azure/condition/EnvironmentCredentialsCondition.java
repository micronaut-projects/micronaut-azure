/*
 * Copyright 2017-2022 original authors
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

import io.micronaut.context.condition.Condition;
import io.micronaut.context.condition.ConditionContext;

import java.util.Objects;
import java.util.stream.Stream;

import static com.azure.core.util.Configuration.*;

/**
 * <p>A custom condition that matches when the following environment variables are defined.</p>
 * <ul>
 *     <li><code>AZURE_CLIENT_ID</code></li>
 *     <li><code>AZURE_CLIENT_SECRET</code></li>
 *     <li><code>AZURE_TENANT_ID</code></li>
 * </ul>
 * <p>or:</p>
 * <ul>
 *     <li><code>AZURE_CLIENT_ID</code></li>
 *     <li><code>AZURE_CLIENT_CERTIFICATE_PATH</code></li>
 *     <li><code>AZURE_TENANT_ID</code></li>
 * </ul>
 * <p>or:</p>
 * <ul>
 *     <li><code>AZURE_CLIENT_ID</code></li>
 *     <li><code>AZURE_USERNAME</code></li>
 *     <li><code>AZURE_PASSWORD</code></li>
 * </ul>
 *
 * @author Álvaro Sánchez-Mariscal
 * @since 3.3.1
 */
public class EnvironmentCredentialsCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context) {
        return hasClientSecret() || hasCliendCertificate() || hasUsernameAndPassword();
    }

    private boolean hasClientSecret() {
        return Stream.of(PROPERTY_AZURE_CLIENT_ID, PROPERTY_AZURE_CLIENT_SECRET, PROPERTY_AZURE_TENANT_ID)
            .map(System::getenv)
            .allMatch(Objects::nonNull);
    }

    private boolean hasCliendCertificate() {
        return Stream.of(PROPERTY_AZURE_CLIENT_ID, PROPERTY_AZURE_CLIENT_CERTIFICATE_PATH, PROPERTY_AZURE_TENANT_ID)
            .map(System::getenv)
            .allMatch(Objects::nonNull);
    }

    private boolean hasUsernameAndPassword() {
        return Stream.of(PROPERTY_AZURE_CLIENT_ID, PROPERTY_AZURE_USERNAME, PROPERTY_AZURE_PASSWORD)
            .map(System::getenv)
            .allMatch(Objects::nonNull);
    }

}
