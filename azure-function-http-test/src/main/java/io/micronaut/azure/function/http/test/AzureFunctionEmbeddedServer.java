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
package io.micronaut.azure.function.http.test;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatusType;
import io.micronaut.azure.function.http.AzureFunctionHttpRequest;
import io.micronaut.azure.function.http.HttpRequestMessageBuilder;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.env.Environment;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.io.IOUtils;
import io.micronaut.core.io.socket.SocketUtils;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.context.ServerContextPathProvider;
import io.micronaut.http.server.HttpServerConfiguration;
import io.micronaut.http.server.exceptions.HttpServerException;
import io.micronaut.http.server.exceptions.ServerStartupException;
import io.micronaut.runtime.ApplicationConfiguration;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.servlet.http.ServletExchange;
import io.micronaut.servlet.http.ServletHttpHandler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.ContextHandler;

import jakarta.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.util.Enumeration;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Server used for testing Azure HTTP functions.
 *
 * @author gkrocher
 * @since 2.0.0
 */
@Singleton
@Internal
final class AzureFunctionEmbeddedServer implements EmbeddedServer {
    private final ApplicationContext applicationContext;
    private final boolean randomPort;
    private final ServerContextPathProvider contextPathProvider;
    private int port;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Server server;
    private String contextPath;

    /**
     * Default cosntructor.
     * @param applicationContext the app context
     * @param httpServerConfiguration the http server configuration
     * @param contextPathProvider THe context path provider
     */
    AzureFunctionEmbeddedServer(
            ApplicationContext applicationContext,
            HttpServerConfiguration httpServerConfiguration,
            ServerContextPathProvider contextPathProvider) {
        this.applicationContext = applicationContext;
        this.contextPathProvider = contextPathProvider;
        Optional<Integer> port = httpServerConfiguration.getPort();
        if (port.isPresent()) {
            this.port = port.get();
            if (this.port == -1) {
                this.port = SocketUtils.findAvailableTcpPort();
                this.randomPort = true;
            } else {
                this.randomPort = false;
            }
        } else {
            if (applicationContext.getEnvironment().getActiveNames().contains(Environment.TEST)) {
                this.randomPort = true;
                this.port = SocketUtils.findAvailableTcpPort();
            } else {
                this.randomPort = false;
                this.port = 8080;
            }
        }
    }

    @Override
    public EmbeddedServer start() {
        if (running.compareAndSet(false, true)) {
            int retryCount = 0;
            while (retryCount <= 3) {
                try {
                    this.server = new Server(port);
                    ContextHandler context = new ContextHandler();
                    this.contextPath = contextPathProvider.getContextPath();
                    if (contextPath == null) {
                        contextPath = "/api";
                    }
                    context.setContextPath(contextPath);
                    context.setResourceBase(".");
                    context.setClassLoader(Thread.currentThread().getContextClassLoader());
                    context.setHandler(new AzureHandler(getApplicationContext(), contextPath));
                    server.setHandler(context);
                    this.server.setHandler(context);
                    this.server.start();
                    break;
                } catch (BindException e) {
                    if (randomPort) {
                        this.port = SocketUtils.findAvailableTcpPort();
                        retryCount++;
                    } else {
                        throw new ServerStartupException(e.getMessage(), e);
                    }
                } catch (Exception e) {
                    throw new ServerStartupException(e.getMessage(), e);
                }
            }
            if (server == null) {
                throw new HttpServerException("No available ports");
            }
        }
        return this;
    }

    @Override
    public EmbeddedServer stop() {
        if (running.compareAndSet(true, false)) {
            try {
                server.stop();
            } catch (Exception e) {
                // ignore / unrecoverable
            }
        }
        return this;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String getHost() {
        return "localhost";
    }

    @Override
    public String getScheme() {
        return "http";
    }

    @Override
    public URL getURL() {
        String spec = getScheme() + "://" + getHost() + ":" + getPort();
        try {
            return new URL(spec);
        } catch (MalformedURLException e) {
            throw new HttpServerException("Invalid server URL " + spec);
        }
    }

    @Override
    public URI getURI() {
        try {
            return getURL().toURI();
        } catch (URISyntaxException e) {
            throw new HttpServerException("Invalid server URL " + getURL());
        }
    }

    @Override
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public ApplicationConfiguration getApplicationConfiguration() {
        return applicationContext.getBean(ApplicationConfiguration.class);
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    /**
     * Internal handler.
     */
    private static final class AzureHandler extends AbstractHandler {

        private final ServletHttpHandler<HttpRequestMessage<Optional<String>>, HttpResponseMessage> httpHandler;
        private final String contextPath;

        /**
         * Default constructor.
         * @param applicationContext The app context
         * @param contextPath The context path
         */
        AzureHandler(ApplicationContext applicationContext, String contextPath) {
            this.contextPath = contextPath;
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
        }

        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
            HttpRequestMessageBuilder<Object> requestMessageBuilder = HttpRequestMessageBuilder.builder(
                    com.microsoft.azure.functions.HttpMethod.value(request.getMethod()),
                    request.getRequestURI(),
                    httpHandler.getApplicationContext()
            );
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String s = headerNames.nextElement();
                Enumeration<String> headers = request.getHeaders(s);
                while (headers.hasMoreElements()) {
                    String v = headers.nextElement();
                    requestMessageBuilder.header(s, v);
                }
            }
            Enumeration<String> parameterNames = request.getParameterNames();
            while (parameterNames.hasMoreElements()) {
                String s = parameterNames.nextElement();
                Enumeration<String> headers = request.getHeaders(s);
                while (headers.hasMoreElements()) {
                    String v = headers.nextElement();
                    requestMessageBuilder.parameter(s, v);
                }
            }


            HttpMethod httpMethod = HttpMethod.parse(request.getMethod());
            if (HttpMethod.permitsRequestBody(httpMethod)) {
                try (BufferedReader requestBody = request.getReader()) {
                    String body = IOUtils.readText(requestBody);
                    requestMessageBuilder.body(body);
                } catch (IOException e) {
                    // ignore
                }
            }

            HttpRequestMessage<Optional<String>> requestMessage = requestMessageBuilder.buildEncoded();
            AzureFunctionHttpRequest<?> azureFunctionHttpRequest =
                    new AzureFunctionHttpRequest<>(
                            contextPath,
                            requestMessage,
                            httpHandler.getMediaTypeCodecRegistry(),
                            new DefaultExecutionContext()
                    );

            ServletExchange<HttpRequestMessage<Optional<String>>, HttpResponseMessage> exchange =
                    httpHandler.exchange(azureFunctionHttpRequest);

            HttpResponseMessage httpResponseMessage = exchange.getResponse().getNativeResponse();
            HttpStatusType httpStatus = httpResponseMessage.getStatus();
            byte[] bodyAsBytes = (byte[]) httpResponseMessage.getBody();
            response.setStatus(httpStatus.value());
            final boolean hasBody = bodyAsBytes != null;
            response.setContentLength(hasBody ? bodyAsBytes.length : 0);
            if (httpResponseMessage instanceof HttpHeaders) {
                HttpHeaders headers = (HttpHeaders) httpResponseMessage;
                headers.forEach((name, values) -> {
                    for (String value : values) {
                        response.addHeader(name, value);
                    }
                });
            }
            if (hasBody && bodyAsBytes.length > 0) {
                try (OutputStream responseBody = response.getOutputStream()) {
                    responseBody.write(bodyAsBytes);
                    responseBody.flush();
                }
            } else {
                response.flushBuffer();
            }
        }
    }

    /**
     * Default execution context impl. used for testing.
     */
    private static class DefaultExecutionContext implements ExecutionContext {

        @Override
        public Logger getLogger() {
            return LogManager.getLogManager().getLogger(AzureFunctionEmbeddedServer.class.getName());
        }

        @Override
        public String getInvocationId() {
            return getFunctionName();
        }

        @Override
        public String getFunctionName() {
            return "io.micronaut.azure.function.http.AzureHttpFunction";
        }
    }
}
