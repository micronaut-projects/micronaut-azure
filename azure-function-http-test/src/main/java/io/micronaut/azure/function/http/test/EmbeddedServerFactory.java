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
package io.micronaut.azure.function.http.test;

import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.sun.net.httpserver.HttpHandler;
import io.micronaut.azure.function.http.HttpRequestMessageHandler;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextProvider;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.annotation.Experimental;
import io.micronaut.core.annotation.Internal;
import io.micronaut.servlet.http.ServletExchange;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

import java.util.Optional;

@Experimental
@Internal
@Factory
class EmbeddedServerFactory {
    @Named("HttpServer")
    @Singleton
    ApplicationContextProvider httpServerApplicationContextProvider(ApplicationContext applicationContext) {
        return createHttpHandler(applicationContext);
    }

    @Singleton
    HttpHandler createHandler(@Named("HttpServer") ApplicationContextProvider applicationContextProvider) {
        if (applicationContextProvider instanceof HttpRequestMessageHandler function) {
            return new AzureFunctionHttpHandler(function);
        }
        throw new ConfigurationException("ApplicationContextProvider with name qualifier HttpServer should be of type AzureFunctionHttpHandler");
    }

    private static HttpRequestMessageHandler createHttpHandler(ApplicationContext applicationContext) {
        return new HttpRequestMessageHandler(applicationContext) {
            @Override
            public boolean isRunning() {
                return applicationContext.isRunning();
            }

            @Override
            protected ServletExchange<HttpRequestMessage<Optional<String>>, HttpResponseMessage> createExchange(HttpRequestMessage<Optional<String>> request, HttpResponseMessage response) {
                throw new UnsupportedOperationException("Creating the exchange directly is not supported");
            }
        };
    }
}
