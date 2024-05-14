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
package io.micronaut.azure.function;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.context.ApplicationContextProvider;
import io.micronaut.context.env.Environment;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.runtime.exceptions.ApplicationStartupException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;

/**
 * A base Azure function class that sets up the Azure environment and preferred configuration.
 *
 * @author graemerocher
 * @since 1.0.0
 */
public abstract class AzureFunction implements ApplicationContextProvider, Closeable {

    protected static final Logger LOG = LoggerFactory.getLogger(AzureFunction.class);
    protected static ApplicationContext applicationContext;

    /**
     * Default constructor.
     */
    protected AzureFunction() {
        this(null);
    }

    /**
     *
     * @param applicationContextBuilder ApplicationContext Builder;
     */
    protected AzureFunction(ApplicationContextBuilder applicationContextBuilder) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Initializing Azure function");
        }
        try {
            startApplicationContext(applicationContextBuilder);
        } catch (Throwable  e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Error initializing Azure function: " + e.getMessage(), e);
            }
            throw new ApplicationStartupException("Error initializing Azure function: " + e.getMessage(), e);
        }
        registerApplicationContextShutDownHook();
        applicationContext.inject(this);
    }

    /**
     * Provides a builder for the ApplicationContext used for the application.
     * This can be overridden to enable customization of the ApplicationContext if needed.
     *
     * @return the builder
     */
    @NonNull
    public static ApplicationContextBuilder defaultApplicationContextBuilder() {
        return ApplicationContext.builder(Environment.AZURE, Environment.FUNCTION).deduceEnvironment(false);
    }

    @Override
    public ApplicationContext getApplicationContext() {
        LOG.trace("getApplicationContext() called. Returning: {}", applicationContext);
        return applicationContext;
    }

    @Override
    public void close() {
        LOG.trace("Closing Azure Function");
        if (applicationContext != null) {
            applicationContext.close();
            applicationContext = null;
        }
    }

    public void startApplicationContext(ApplicationContextBuilder applicationContextBuilder) {
        if (applicationContext == null) {
            applicationContext = (applicationContextBuilder != null ? applicationContextBuilder : defaultApplicationContextBuilder()).build();
            applicationContext.start();
        }
    }

    /**
     * Registers an applicationContextShutdownHook.
     */
    protected void registerApplicationContextShutDownHook() {
        Runtime.getRuntime().addShutdownHook(createApplicationContextShutDownHook());
    }

    /**
     *
     * @return A new Thread which closes and sets to null the application context if not null
     */
    private Thread createApplicationContextShutDownHook() {
        return new Thread(this::close);
    }
}
