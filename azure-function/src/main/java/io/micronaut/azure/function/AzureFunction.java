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
import io.micronaut.context.ApplicationContextProvider;
import io.micronaut.context.env.Environment;

import java.io.Closeable;
import java.io.IOException;

/**
 * A base Azure function class that sets up the Azure environment and preferred configuration.
 *
 * @author graemerocher
 * @since 1.0.0
 */
public abstract class AzureFunction implements ApplicationContextProvider, Closeable {
    protected static ApplicationContext applicationContext;

    static {
        startApplicationContext();
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

    private static void startApplicationContext() {
        applicationContext = ApplicationContext.builder(Environment.AZURE, Environment.FUNCTION)
                .deduceEnvironment(false)
                .build();
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
