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
package io.micronaut.azure.function.http;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import io.micronaut.azure.function.AzureFunction;
import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.function.BinaryTypeConfiguration;
import io.micronaut.servlet.http.BodyBuilder;
import io.micronaut.servlet.http.ServletExchange;
import io.micronaut.servlet.http.ServletHttpHandler;

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

    protected ServletHttpHandler<HttpRequestMessage<Optional<String>>, HttpResponseMessage> httpHandler;

    /**
     * Default constructor.
     */
    protected AzureHttpFunction() {
        this(null);
    }

    /**
     *
     * @param applicationContextBuilder ApplicationContext Builder;
     */
    public AzureHttpFunction(ApplicationContextBuilder applicationContextBuilder) {
        super(applicationContextBuilder);
        if (LOG.isTraceEnabled()) {
            LOG.trace("Initializing AzureHttpFunction");
        }
        httpHandler = new HttpHandler(getApplicationContext());
        registerHttpHandlerShutDownHook();
    }

    /**
     * Entry point for Azure functions written in Micronaut.
     *
     * @param request The request
     * @param executionContext The execution context
     * @return The response message
     */
    public HttpResponseMessage route(
        HttpRequestMessage<Optional<String>> request,
        ExecutionContext executionContext
    ) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("Route request: {}", request);
        }
        try {
            AzureFunctionHttpRequest<?> azureFunctionHttpRequest =
                new AzureFunctionHttpRequest<>(
                    request,
                    new AzureFunctionHttpResponse<>(
                        request,
                        httpHandler.getApplicationContext().getBean(ConversionService.class),
                        httpHandler.getApplicationContext().getBean(BinaryTypeConfiguration.class)
                    ),
                    executionContext,
                    httpHandler.getApplicationContext().getBean(ConversionService.class),
                    httpHandler.getApplicationContext().getBean(BinaryTypeConfiguration.class),
                    httpHandler.getApplicationContext().getBean(BodyBuilder.class)
                );

            ServletExchange<HttpRequestMessage<Optional<String>>, HttpResponseMessage> exchange =
                httpHandler.exchange(azureFunctionHttpRequest);

            return exchange.getResponse().getNativeResponse();
        } finally {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Request complete, destroying request bean {}", this);
            }
            applicationContext.destroyBean(this);
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

    private void registerHttpHandlerShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> httpHandler = null));
    }
}
