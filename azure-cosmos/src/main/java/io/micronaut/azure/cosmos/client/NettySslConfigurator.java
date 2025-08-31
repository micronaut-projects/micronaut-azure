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
package io.micronaut.azure.cosmos.client;

import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.StringUtils;
import jakarta.annotation.PostConstruct;

/**
 * Configures Netty's SSL context based on the provided {@link NettySslConfiguration}.
 * <p>
 * This class is responsible for setting the default endpoint verification algorithm system property
 * used by Netty's {@link io.netty.handler.ssl.SslContextBuilder} if it is not already set.
 * </p>
 */
@Context
@Internal
@Requires(beans = NettySslConfiguration.class)
public class NettySslConfigurator {

    /**
     * The system property key for the default endpoint verification algorithm used by Netty's SSL context.
     */
    private static final String NETTY_HANDLER_SSL_DEFAULT_ENDPOINT_VERIFICATION_ALGORITHM = "io.netty.handler.ssl.defaultEndpointVerificationAlgorithm";

    /**
     * Initializes the Netty SSL configuration based on the provided {@link NettySslConfiguration}.
     * <p>
     * If the provided configuration is not null and has a non-empty default endpoint verification algorithm,
     * this method sets the corresponding system property if it is not already set.
     * </p>
     *
     * @param nettySslConfiguration the Netty SSL configuration to use, or null if not available
     */
    @PostConstruct
    public void initNettySslConfiguration(@Nullable NettySslConfiguration nettySslConfiguration) {
        if (nettySslConfiguration == null) {
            return;
        }

        if (StringUtils.isNotEmpty(nettySslConfiguration.defaultEndpointVerificationAlgorithm())) {
            final String defaultEndpointVerificationAlgorithm = System.getProperty(NETTY_HANDLER_SSL_DEFAULT_ENDPOINT_VERIFICATION_ALGORITHM);
            if (StringUtils.isEmpty(defaultEndpointVerificationAlgorithm)) {
                System.setProperty(
                    NETTY_HANDLER_SSL_DEFAULT_ENDPOINT_VERIFICATION_ALGORITHM,
                    nettySslConfiguration.defaultEndpointVerificationAlgorithm());
            }
        }
    }
}
