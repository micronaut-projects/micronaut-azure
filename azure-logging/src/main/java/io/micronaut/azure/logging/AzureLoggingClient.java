/*
 * Copyright 2017-2024 original authors
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
package io.micronaut.azure.logging;

import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.core.annotation.Internal;
import io.micronaut.runtime.ApplicationConfiguration;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Singleton;

import static io.micronaut.core.util.StringUtils.TRUE;

/**
 * Log ingestion client used by {@link AzureAppender}.
 *
 * @since 5.6
 */
@Context
@Internal
@Singleton
@Requires(property = AzureLoggingClient.ENABLED, value = TRUE, defaultValue = TRUE)
final class AzureLoggingClient implements ApplicationEventListener<ServerStartupEvent> {

    public static final String ENABLED = AzureLoggingConfigurationProperties.PREFIX + ".enabled";

    private static String appName;
    private static String host;
    private static ClientWrapper client;

    private final String internalAppName;
    private final ClientWrapper internalClient;

    AzureLoggingClient(ApplicationConfiguration appConfig,
                       ClientWrapper clientWrapper) {
        internalAppName = appConfig.getName().orElse("");
        internalClient = clientWrapper;
    }

    static synchronized boolean isReady() {
        return client != null;
    }

    static synchronized String getHost() {
        return host;
    }

    static synchronized String getAppName() {
        return appName;
    }

    private static synchronized void setLogging(ClientWrapper client,
                                                String host,
                                                String appName) {
        AzureLoggingClient.client = client;
        AzureLoggingClient.host = host;
        AzureLoggingClient.appName = appName;
    }

    static synchronized void destroy() {
        AzureLoggingClient.client.close();
        AzureLoggingClient.client = null;
        AzureLoggingClient.host = null;
        AzureLoggingClient.appName = null;
    }

    static synchronized boolean sendLogs(Iterable<Object> entries) {
        if (client == null) {
            return false;
        }

        client.upload(entries);
        return true;
    }

    @PreDestroy
    public void close() {
        AzureLoggingClient.destroy();
    }

    @Override
    public void onApplicationEvent(ServerStartupEvent event) {
        setLogging(internalClient, event.getSource().getHost(), internalAppName);
    }
}
