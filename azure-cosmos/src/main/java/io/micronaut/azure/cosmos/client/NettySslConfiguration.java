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

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Nullable;

import static io.micronaut.azure.cosmos.client.NettySslConfiguration.PREFIX;

/**
 * Configuration properties for Netty {@link io.netty.handler.ssl.SslContextBuilder}.
 *
 * @param defaultEndpointVerificationAlgorithm Sets default endpoint verification algorithm system property.
 *                                             If system property is not set, Netty {@link io.netty.handler.ssl.SslContextBuilder} will
 *                                             assume HTTPS is default value.
 *
 */
@ConfigurationProperties(PREFIX)
@Requires(property = PREFIX)
public record NettySslConfiguration(@Nullable String defaultEndpointVerificationAlgorithm) {

    /** Prefix used for configuration properties. */
    static final String PREFIX = "io.netty.handler.ssl";
}
