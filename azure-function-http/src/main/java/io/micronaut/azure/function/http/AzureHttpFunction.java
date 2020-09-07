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
package io.micronaut.azure.function.http;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import io.micronaut.azure.function.AzureFunction;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.context.ServerContextPathProvider;
import io.micronaut.servlet.http.ServletExchange;
import io.micronaut.servlet.http.ServletHttpHandler;
import io.netty.util.internal.MacAddressUtil;
import io.netty.util.internal.PlatformDependent;

import java.util.Objects;
import java.util.Optional;

/**
 * This class can be used as a base class for Azure HTTP functions that wish to route requests
 * to classes annotated with {@link io.micronaut.http.annotation.Controller} and the others annotations in the
 * {@link io.micronaut.http.annotation} package.
 *
 * <p>To use this class you should define a new function that subclasses this class and then override the {@link #route(HttpRequestMessage, ExecutionContext)} (HttpRequestMessage, ExecutionContext)} method to customize the function mapping as per the Azure documentation. For example the following definition will route all requests to the function:</p>
 *
 * <pre>{@code
 *    @FunctionName("myFunction")
 *    @Override
 *    public HttpResponseMessage invoke(
 *       @HttpTrigger(name = "req",
 *                    route = "{*url}", // catch all route
 *                    authLevel = AuthorizationLevel.ANONYMOUS)
 *       HttpRequestMessage<Optional<String>> request,
 *       ExecutionContext executionContext) {
 *
 *    }
 *
 * }</pre>
 *
 * @author graemerocher
 * @since 1.0.0
 */
public class AzureHttpFunction extends AzureFunction {
    protected static ServletHttpHandler<HttpRequestMessage<Optional<String>>, HttpResponseMessage> httpHandler;

    static {
        byte[] bestMacAddr = new byte[8];
        PlatformDependent.threadLocalRandom().nextBytes(bestMacAddr);
        System.setProperty("io.netty.machineId", MacAddressUtil.formatAddress(bestMacAddr));

        httpHandler = new ServletHttpHandler<HttpRequestMessage<Optional<String>>, HttpResponseMessage>(applicationContext) {
            @Override
            public boolean isRunning() {
                return applicationContext.isRunning();
            }

            @Override
            protected ServletExchange<HttpRequestMessage<Optional<String>>, HttpResponseMessage> createExchange(HttpRequestMessage<Optional<String>> request, HttpResponseMessage response) {
                throw new UnsupportedOperationException("Creating the exchange directly is not supported");
            }
        };

        Runtime.getRuntime().addShutdownHook(
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        httpHandler = null;
                    }
                })
        );
    }

    private final String contextPath;

    /**
     * Default constructor.
     */
    public AzureHttpFunction() {
        applicationContext.registerSingleton(this);
        this.contextPath = applicationContext.findBean(ServerContextPathProvider.class).map(ServerContextPathProvider::getContextPath).orElse("/api");
    }

    /**
     * Entry point for Azure functions written in Micronaut.
     *
     * @param request The request
     * @param executionContext The execution context
     * @return THe response message
     */
    public HttpResponseMessage route(
            HttpRequestMessage<Optional<String>> request,
            ExecutionContext executionContext) {
        try {
            AzureFunctionHttpRequest<?> azureFunctionHttpRequest =
                    new AzureFunctionHttpRequest<>(
                            contextPath,
                            request,
                            httpHandler.getMediaTypeCodecRegistry(),
                            executionContext
                    );

            ServletExchange<HttpRequestMessage<Optional<String>>, HttpResponseMessage> exchange =
                    httpHandler.exchange(azureFunctionHttpRequest);

            return exchange.getResponse().getNativeResponse();
        } finally {
            applicationContext.destroyBean(getClass());
        }
    }

    /**
     * Start a new request.
     * @param method The method
     * @param uri The URI
     * @return The builder
     */
    public HttpRequestMessageBuilder<?> request(HttpMethod method, String uri) {
        Objects.requireNonNull(uri, "The URI cannot be null");
        uri = StringUtils.prependUri(contextPath, uri);
        return HttpRequestMessageBuilder.builder(method, uri, applicationContext);
    }

    /**
     * Start a new request.
     * @param method The method
     * @param uri The URI
     * @return The builder
     */
    public HttpRequestMessageBuilder<?> request(io.micronaut.http.HttpMethod method, String uri) {
        Objects.requireNonNull(method, "The method cannot be null");
        return request(HttpMethod.value(method.name()), uri);
    }
}
