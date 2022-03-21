/*
 * Copyright 2017-2020 original authors
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
package io.micronaut.azure.secretmanager;

import com.azure.security.keyvault.secrets.SecretClient;
import io.micronaut.azure.secretmanager.client.SecretKeyvaultClient;
import io.micronaut.azure.secretmanager.client.VersionedSecret;
import io.micronaut.azure.secretmanager.configuration.AzureKeyvaultConfigurationProperties;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.context.env.PropertySource;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.discovery.config.ConfigurationClient;
import io.micronaut.scheduling.TaskExecutors;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * Distributed configuration client implementation that fetches application secret values from Azure keyvalut.
 */
@Singleton
@Requires(beans = SecretClient.class)
@BootstrapContextCompatible
public class AzureVaultConfigurationClient implements ConfigurationClient {

    private static final Logger LOG = LoggerFactory.getLogger(AzureVaultConfigurationClient.class);

    private final AzureKeyvaultConfigurationProperties azureKeyvaultConfigurationProperties;
    private final ExecutorService executorService;
    private final SecretKeyvaultClient secretClient;

    /**
     * Default Constructor.
     *
     * @param azureKeyvaultConfigurationProperties Azure Secret Vault Client Configuration
     * @param executorService                      Executor Service
     * @param secretClient                         The secrets client
     */
    public AzureVaultConfigurationClient(
            AzureKeyvaultConfigurationProperties azureKeyvaultConfigurationProperties,
            @Named(TaskExecutors.IO) @Nullable ExecutorService executorService,
            SecretKeyvaultClient secretClient) {
        this.azureKeyvaultConfigurationProperties = azureKeyvaultConfigurationProperties;
        this.executorService = executorService;
        this.secretClient = secretClient;
    }

    @Override
    public Publisher<PropertySource> getPropertySources(Environment environment) {

        if (azureKeyvaultConfigurationProperties.getVaultURL() == null || azureKeyvaultConfigurationProperties.getVaultURL().equals("")) {
            return Flux.empty();
        }

        List<Flux<PropertySource>> propertySources = new ArrayList<>();
        Scheduler scheduler = executorService != null ? Schedulers.fromExecutor(executorService) : null;

        Map<String, Object> secrets = new HashMap<>();

        int retrieved = 0;

        for (VersionedSecret versionedSecret : secretClient.listSecrets()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Retrieving secrets from Azure Secret Vault with URL: {}", azureKeyvaultConfigurationProperties.getVaultURL());
            }
            retrieved += 1;
            secrets.put(
                    versionedSecret.getName(),
                    versionedSecret.getValue()
            );
            secrets.put(
                    versionedSecret.getName().replace('-', '.'),
                    versionedSecret.getValue()
            );
            secrets.put(
                    versionedSecret.getName().replace('-', '_'),
                    versionedSecret.getValue()
            );
            if (LOG.isTraceEnabled()) {
                LOG.trace("Retrieved secret: {}", versionedSecret.getName());
            }

        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("{} secrets where retrieved from Azure Secret Vault with URL: {}", retrieved, azureKeyvaultConfigurationProperties.getVaultURL());
        }

        Flux<PropertySource> propertySourceFlowable = Flux.just(
                PropertySource.of(secrets)
        );

        if (scheduler != null) {
            propertySourceFlowable = propertySourceFlowable.subscribeOn(scheduler);
        }
        propertySources.add(propertySourceFlowable);
        return Flux.merge(propertySources);
    }

    @Override
    public String getDescription() {
        return "Retrieves secrets from Azure key vaults";
    }
}
