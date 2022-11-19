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
import java.io.IOException;

/**
 * A base Azure function class that sets up the Azure environment and preferred configuration.
 *
 * @author graemerocher
 * @since 1.0.0
 */
public abstract class AzureFunction implements ApplicationContextProvider, Closeable {
    protected static final Logger LOG = LoggerFactory.getLogger(AzureFunction.class);
    protected static ApplicationContext applicationContext;

    static {
        try {
            startApplicationContext();
        } catch (Throwable  e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Error initializing Azure function: " + e.getMessage(), e);
            }
            throw new ApplicationStartupException("Error initializing Azure function: " + e.getMessage(), e);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                if (applicationContext != null) {
                    applicationContext.close();
                }
                applicationContext = null;
            }
        }));
    }

    /**
     * Default constructor.
     */
    protected AzureFunction() {
        if (applicationContext == null) {
            startApplicationContext();
        }
        applicationContext.inject(this);
    }

    /**
     * Provides a builder for the ApplicationContext used for the application.
     * This can be overridden to enable customization of the ApplicationContext if needed.
     *
     * @return the builder
     */
    @NonNull
    protected static ApplicationContextBuilder newApplicationContextBuilder() {
        return ApplicationContext.builder(Environment.AZURE, Environment.FUNCTION).deduceEnvironment(false);
    }

    private static void startApplicationContext() {
        applicationContext = newApplicationContextBuilder().build();
        applicationContext.start();
    }

    @Override
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public void close() throws IOException {
        if (applicationContext != null) {
            applicationContext.close();
            applicationContext = null;
        }
    }
}
