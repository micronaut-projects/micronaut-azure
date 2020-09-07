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

import com.microsoft.azure.functions.*;
import io.micronaut.azure.function.AzureFunction;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.MediaType;
import io.micronaut.http.codec.MediaTypeCodec;
import io.micronaut.http.codec.MediaTypeCodecRegistry;
import io.micronaut.http.context.ServerContextPathProvider;
import io.micronaut.servlet.http.ServletExchange;
import io.micronaut.servlet.http.ServletHttpHandler;
import io.netty.util.internal.MacAddressUtil;
import io.netty.util.internal.PlatformDependent;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.LogManager;
import java.util.logging.Logger;

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
        return new DefaultHttpRequestMessageBuilder<>(method, URI.create(uri));
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

    /**
     * Internal class for building request messages.
     * @param <T> The body type
     */
    private class DefaultHttpRequestMessageBuilder<T> implements HttpRequestMessageBuilder<T>, HttpRequestMessage<T> {

        private HttpMethod method = HttpMethod.GET;
        private URI uri;
        private final Map<String, String> headers = new LinkedHashMap<>(3);
        private final Map<String, String> queryParams = new LinkedHashMap<>(3);
        private Object body;

        public DefaultHttpRequestMessageBuilder(HttpMethod method, URI uri) {
            method(method);
            uri(uri);
        }

        @Override
        public HttpRequestMessageBuilder<T> method(HttpMethod method) {
            this.method = Objects.requireNonNull(method, "The method cannot be null");
            return this;
        }

        @Override
        public HttpRequestMessageBuilder<T> uri(URI uri) {
            this.uri = Objects.requireNonNull(uri, "The URI cannot be null");
            return this;
        }

        @Override
        public HttpRequestMessageBuilder<T> header(String name, String value) {
            String headerName = Objects.requireNonNull(name, "The name cannot be null");
            if (value == null) {
                headers.remove(headerName);
            } else {
                headers.put(
                        headerName,
                        value
                );
            }
            return this;
        }

        @Override
        public HttpRequestMessageBuilder<T> parameter(String name, String value) {
            String headerName = Objects.requireNonNull(name, "The name cannot be null");
            if (value == null) {
                queryParams.remove(headerName);
            } else {
                queryParams.put(
                        headerName,
                        value
                );
            }
            return this;
        }

        @Override
        public <B> HttpRequestMessageBuilder<B> body(B body) {
            this.body = body;
            return (HttpRequestMessageBuilder<B>) this;
        }

        @Override
        public HttpRequestMessage<T> build() {
            return this;
        }

        @Override
        public AzureHttpResponseMessage invoke() {
            HttpResponseMessage result = AzureHttpFunction.this.route(
                    buildEncodedRequest(),
                    new DefaultExecutionContext()
            );
            return new AzureHttpResponseMessage() {
                @Override
                public HttpStatusType getStatus() {
                    return result.getStatus();
                }

                @Override
                public String getHeader(String key) {
                    return result.getHeader(key);
                }

                @Override
                public Object getBody() {
                    return result.getBody();
                }
            };
        }

        private HttpRequestMessage<Optional<String>> buildEncodedRequest() {
            if (this.body != null) {
                if (this.body instanceof byte[]) {
                    this.body = Optional.of((byte[]) this.body);
                } else if (this.body instanceof CharSequence) {
                    this.body = Optional.of(this.body.toString());
                } else {
                    MediaTypeCodecRegistry codecRegistry = AzureHttpFunction.this.httpHandler.getMediaTypeCodecRegistry();
                    String ct = getHeaders().get(HttpHeaders.CONTENT_TYPE);
                    MediaType mediaType = MediaType.APPLICATION_JSON_TYPE;
                    if (ct != null) {
                        mediaType = new MediaType(ct);
                    }
                    MediaTypeCodec codec = codecRegistry.findCodec(mediaType, body.getClass()).orElse(null);
                    if (codec != null) {
                        this.body = Optional.of(new String(codec.encode(body), StandardCharsets.UTF_8));
                    } else {
                        this.body = Optional.of(this.body.toString());
                    }
                }
            } else {
                this.body = Optional.empty();
            }
            return (HttpRequestMessage<Optional<String>>) this;
        }

        @Override
        public URI getUri() {
            return uri;
        }

        @Override
        public HttpMethod getHttpMethod() {
            return method;
        }

        @Override
        public Map<String, String> getHeaders() {
            return headers;
        }

        @Override
        public Map<String, String> getQueryParameters() {
            return queryParams;
        }

        @Override
        public T getBody() {
            return (T) body;
        }

        @Override
        public HttpResponseMessage.Builder createResponseBuilder(HttpStatus status) {
            return new ResponseBuilder().status(status);
        }

        @Override
        public HttpResponseMessage.Builder createResponseBuilder(HttpStatusType status) {
            return new ResponseBuilder().status(status);
        }
    }

    /**
     * Response builder implementation. Used for testing.
     */
    private static class ResponseBuilder implements HttpResponseMessage.Builder, HttpResponseMessage {
        private HttpStatusType status = HttpStatus.OK;
        private final Map<String, String> headers = new LinkedHashMap<>(3);
        private Object body;

        @Override
        public HttpResponseMessage.Builder status(HttpStatusType status) {
            this.status = status;
            return this;
        }

        @Override
        public HttpResponseMessage.Builder header(String key, String value) {
            headers.put(key, value);
            return this;
        }

        @Override
        public HttpResponseMessage.Builder body(Object body) {
            this.body = body;
            return this;
        }

        @Override
        public HttpResponseMessage build() {
            return this;
        }

        @Override
        public HttpStatusType getStatus() {
            return status;
        }

        @Override
        public String getHeader(String key) {
            return headers.get(key);
        }

        @Override
        public Object getBody() {
            return this.body;
        }
    }

    /**
     * Default execution context impl. used for testing.
     */
    private class DefaultExecutionContext implements ExecutionContext {

        @Override
        public Logger getLogger() {
            return LogManager.getLogManager().getLogger(AzureHttpFunction.class.getName());
        }

        @Override
        public String getInvocationId() {
            return getFunctionName();
        }

        @Override
        public String getFunctionName() {
            return AzureHttpFunction.this.getClass().getName();
        }
    }
}
