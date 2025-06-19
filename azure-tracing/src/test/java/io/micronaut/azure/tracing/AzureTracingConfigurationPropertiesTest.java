package io.micronaut.azure.tracing;

import io.micronaut.context.ApplicationContext;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AzureTracingConfigurationPropertiesTest {

    @Test
    void connectionStringCanBePopulatedViaConfiguration() {
        Map<String, Object> config = Map.of(
            "azure.tracing.connection-string", "foobar"
        );
        try (ApplicationContext ctx = ApplicationContext.run(config)) {

            AzureTracingConfigurationProperties tracingConfig =
                ctx.getBean(AzureTracingConfigurationProperties.class);
            assertEquals("foobar", tracingConfig.getConnectionString());
        }
    }

    @Test
    void setterAndGetter() {
        AzureTracingConfigurationProperties tracingConfig =
            new AzureTracingConfigurationProperties();
        tracingConfig.setConnectionString("foobar");
        assertEquals("foobar", tracingConfig.getConnectionString());
    }
}
