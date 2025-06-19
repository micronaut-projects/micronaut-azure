/*
 * Copyright 2017-2025 original authors
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
package io.micronaut.azure.tracing;

import com.azure.monitor.opentelemetry.autoconfigure.AzureMonitorAutoConfigure;
import io.micronaut.tracing.opentelemetry.OpenTelemetryBuilderCustomizer;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdkBuilder;
import jakarta.inject.Singleton;

/**
 * Will be auto-discovered by micronaut-tracing-opentelemetry when building the OpenTelemetry
 * bean. If the Azure Monitor workspace connection string is set in the application configuration,
 * it will be used to connect via AzureMonitorAutoConfigure.
 */
@Singleton
public class OpenTelemetryConfigurer implements OpenTelemetryBuilderCustomizer {

    private final AzureTracingConfigurationProperties configurationProperties;

    OpenTelemetryConfigurer(AzureTracingConfigurationProperties configurationProperties) {
        this.configurationProperties = configurationProperties;
    }

    @Override
    public void configure(AutoConfiguredOpenTelemetrySdkBuilder builder) {
        if (configurationProperties.getConnectionString() != null) {
            AzureMonitorAutoConfigure.customize(builder, configurationProperties.getConnectionString());
        }
    }
}
