package io.micronaut.azure.tracing;

import com.azure.monitor.opentelemetry.autoconfigure.AzureMonitorAutoConfigure;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdkBuilder;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class OpenTelemetryConfigurerTest {

    @Test
    void shouldCustomizeBuilderWhenConnectionStringIsPresent() {
        String connectionString = "test-connection-string";
        AzureTracingConfigurationProperties tracingConfig = new AzureTracingConfigurationProperties();
        tracingConfig.setConnectionString(connectionString);

        AutoConfiguredOpenTelemetrySdkBuilder builder = mock(AutoConfiguredOpenTelemetrySdkBuilder.class);
        OpenTelemetryConfigurer configurer = new OpenTelemetryConfigurer(tracingConfig);
        try (MockedStatic<AzureMonitorAutoConfigure> mockedStatic = mockStatic(AzureMonitorAutoConfigure.class)) {
            configurer.configure(builder);
            mockedStatic.verify(() ->
                AzureMonitorAutoConfigure.customize(eq(builder), eq(connectionString)));
        }
    }

    @Test
    void shouldNotCustomizeBuilderWhenConnectionStringIsNull() {
        AzureTracingConfigurationProperties tracingConfig = new AzureTracingConfigurationProperties();
        AutoConfiguredOpenTelemetrySdkBuilder builder = mock(AutoConfiguredOpenTelemetrySdkBuilder.class);
        OpenTelemetryConfigurer configurer = new OpenTelemetryConfigurer(tracingConfig);
        try (MockedStatic<AzureMonitorAutoConfigure> mockedStatic = mockStatic(AzureMonitorAutoConfigure.class)) {
            configurer.configure(builder);
            mockedStatic.verifyNoInteractions();
        }
    }
}
