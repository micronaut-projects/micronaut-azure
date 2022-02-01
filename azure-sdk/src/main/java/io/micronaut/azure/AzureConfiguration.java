package io.micronaut.azure;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.env.Environment;
import io.micronaut.core.annotation.NonNull;

/**
 * @author Pavol Gressa
 * @since 2.5
 */
@ConfigurationProperties(AzureConfiguration.PREFIX)
public interface AzureConfiguration {

    String PREFIX = Environment.AZURE;

    @NonNull
    String getClientId();
}
