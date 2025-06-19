package io.micronaut.azure.tracing;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AzureTracingConfigurationPropertiesTest {

    @Test
    void connectionStringCanBePopulatedViaConfiguration() {
        AzureTracingConfigurationProperties tracingConfig =
            new AzureTracingConfigurationProperties();
        tracingConfig.setConnectionString("foobar");
        assertEquals("foobar", tracingConfig.getConnectionString());
    }
}
