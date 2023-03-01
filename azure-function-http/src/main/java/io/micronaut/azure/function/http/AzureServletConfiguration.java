/*
 * Copyright 2017-2023 original authors
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
package io.micronaut.azure.function.http;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.env.Environment;
import io.micronaut.servlet.http.ServletConfiguration;

/**
 * Configuration for the servlet environment.
 *
 * @author Tim Yates
 * @since 5.0.0
 */
@ConfigurationProperties(AzureServletConfiguration.PREFIX)
public class AzureServletConfiguration implements ServletConfiguration {

    public static final String PREFIX = Environment.AZURE + ".function.servlet";

    private boolean asyncFileServingEnabled = true;

    /**
     * Is async file serving enabled.
     * @param enabled True if it is
     */
    public void setAsyncFileServingEnabled(boolean enabled) {
        this.asyncFileServingEnabled = enabled;
    }

    @Override
    public boolean isAsyncFileServingEnabled() {
        return asyncFileServingEnabled;
    }
}
