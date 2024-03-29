/*
 * Copyright 2017-2023 original authors
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
import io.micronaut.azure.function.http.AzureFunctionHttpResponse;
import io.micronaut.azure.function.http.HttpRequestMessageBuilder;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.env.Environment;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.io.IOUtils;
import io.micronaut.function.BinaryTypeConfiguration;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.server.HttpServerConfiguration;
import io.micronaut.http.server.exceptions.HttpServerException;
import io.micronaut.http.server.exceptions.ServerStartupException;
import io.micronaut.runtime.ApplicationConfiguration;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.servlet.http.BodyBuilder;
import io.micronaut.servlet.http.ServletExchange;
import io.micronaut.servlet.http.ServletHttpHandler;
import io.micronaut.servlet.http.ServletHttpResponse;
import jakarta.inject.Singleton;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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
    private final ServerPort serverPort;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Server server;

    /**
     * Default constructor.
     * @param applicationContext the app context
     * @param httpServerConfiguration the http server configuration
     */
    AzureFunctionEmbeddedServer(
            ApplicationContext applicationContext,
            HttpServerConfiguration httpServerConfiguration
    ) {
        this.applicationContext = applicationContext;
        this.serverPort = createServerPort(httpServerConfiguration);
    }

    private ServerPort createServerPort(HttpServerConfiguration httpServerConfiguration) {
        Optional<Integer> portOpt = httpServerConfiguration.getPort();
        if (portOpt.isPresent()) {
            Integer port = portOpt.get();
            if (port == -1) {
                return new ServerPort(true, 0);

            } else {
                return new ServerPort(false, port);
            }
        } else {
            if (applicationContext.getEnvironment().getActiveNames().contains(Environment.TEST)) {
                return new ServerPort(true, 0);
            } else {
                return new ServerPort(false, 8080);
            }
        }
    }

    @Override
    public EmbeddedServer start() {
        if (running.compareAndSet(false, true)) {
            int port = serverPort.port();
            try {
                this.server = new Server(port);
                this.server.setHandler(new AzureHandler(applicationContext, applicationContext.getConversionService()));
                this.server.start();
            } catch (Exception e) {
                throw new ServerStartupException(e.getMessage(), e);
            }
        }
        return this;
    }

    @Override
    public EmbeddedServer stop() {
        if (running.compareAndSet(true, false)) {
            try {
                applicationContext.stop();
                server.stop();
            } catch (Exception e) {
                // ignore / unrecoverable
            }
        }
        return this;
    }

    @Override
    public int getPort() {
        return server.getURI().getPort();
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

        /**
         * Default constructor.
         * @param applicationContext The app context
         */
        AzureHandler(ApplicationContext applicationContext, ConversionService conversionService) {
            httpHandler = new ServletHttpHandler<>(applicationContext, conversionService) {
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
                    baseRequest.getOriginalURI(),
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
                String[] parameterValues = request.getParameterValues(s);
                requestMessageBuilder.parameter(s, String.join(",", parameterValues));
            }

            HttpMethod httpMethod = HttpMethod.parse(request.getMethod());
            if (HttpMethod.permitsRequestBody(httpMethod)) {
                try (InputStream inputStream = request.getInputStream();
                     InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                     BufferedReader requestBody = new BufferedReader(inputStreamReader)) {
                    String body = IOUtils.readText(requestBody);
                    requestMessageBuilder.body(body);
                } catch (IOException e) {
                    // ignore
                }
            }

            HttpRequestMessage<Optional<String>> requestMessage = requestMessageBuilder.buildEncoded();
            ConversionService handlerConversionService = httpHandler.getApplicationContext().getBean(ConversionService.class);
            BinaryTypeConfiguration binaryTypeConfiguration = httpHandler.getApplicationContext().getBean(BinaryTypeConfiguration.class);
            AzureFunctionHttpRequest<?> azureFunctionHttpRequest =
                    new AzureFunctionHttpRequest<>(
                            requestMessage,
                            new AzureFunctionHttpResponse<>(
                                requestMessage,
                                handlerConversionService,
                                binaryTypeConfiguration
                            ),
                            new DefaultExecutionContext(),
                            handlerConversionService,
                            binaryTypeConfiguration,
                            httpHandler.getApplicationContext().getBean(BodyBuilder.class)
                    );

            ServletExchange<HttpRequestMessage<Optional<String>>, HttpResponseMessage> exchange =
                    httpHandler.exchange(azureFunctionHttpRequest);

            ServletHttpResponse<HttpResponseMessage, ?> exchangeResponse = exchange.getResponse();
            HttpResponseMessage httpResponseMessage = exchangeResponse.getNativeResponse();
            HttpStatusType httpStatus = httpResponseMessage.getStatus();

            Object bodyObject = httpResponseMessage.getBody();
            byte[] bodyAsBytes = null;
            if (bodyObject instanceof CharSequence charBody) {
                bodyAsBytes = charBody.toString().getBytes(exchangeResponse.getCharacterEncoding());
            } else if (bodyObject instanceof byte[] byteBody) {
                bodyAsBytes = byteBody;
            }
            response.setStatus(httpStatus.value());
            final boolean hasBody = bodyAsBytes != null;
            response.setContentLength(hasBody ? bodyAsBytes.length : 0);
            if (httpResponseMessage instanceof HttpHeaders headers) {
                headers.forEach((name, values) -> {
                    for (String value : values) {
                        if (!response.containsHeader(name)) {
                            response.addHeader(name, value);
                        }
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

    private record ServerPort(boolean random, Integer port) {
    }
}
