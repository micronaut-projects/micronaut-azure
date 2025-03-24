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

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatusType;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.micronaut.azure.function.http.AzureFunctionHttpRequest;
import io.micronaut.azure.function.http.AzureFunctionHttpResponse;
import io.micronaut.azure.function.http.HttpRequestMessageBuilder;
import io.micronaut.azure.function.http.HttpRequestMessageHandler;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextProvider;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.io.IOUtils;
import io.micronaut.function.BinaryTypeConfiguration;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.uri.QueryStringDecoder;
import io.micronaut.servlet.http.BodyBuilder;
import io.micronaut.servlet.http.ServletExchange;
import io.micronaut.servlet.http.ServletHttpResponse;
import jakarta.inject.Singleton;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.logging.LogManager;
import java.util.logging.Logger;

@Internal
@Singleton
public class AzureFunctionHttpHandler implements HttpHandler {
    private final HttpRequestMessageHandler handler;

    public AzureFunctionHttpHandler(HttpRequestMessageHandler handler) {
        this.handler = handler;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        ServletExchange<HttpRequestMessage<Optional<String>>, HttpResponseMessage> servletExchange = createHttpRequest(exchange, handler);
        handler.exchange(servletExchange);
        ServletHttpResponse<HttpResponseMessage, ?> exchangeResponse = servletExchange.getResponse();
        HttpResponseMessage httpResponseMessage = exchangeResponse.getNativeResponse();
        HttpStatusType httpStatus = httpResponseMessage.getStatus();

        Object bodyObject = httpResponseMessage.getBody();
        byte[] bodyAsBytes = null;
        if (bodyObject instanceof CharSequence charBody) {
            bodyAsBytes = charBody.toString().getBytes(exchangeResponse.getCharacterEncoding());
        } else if (bodyObject instanceof byte[] byteBody) {
            bodyAsBytes = byteBody;
        }
        int status = httpStatus.value();
        final boolean hasBody = bodyAsBytes != null;
        int contentLength = hasBody ? bodyAsBytes.length : 0;
        if (httpResponseMessage instanceof HttpHeaders headers) {
            headers.forEach((name, values) -> {
                exchange.getRequestHeaders().put(name, values);
            });
        }
        exchange.sendResponseHeaders(status, contentLength);
        if (hasBody && bodyAsBytes.length > 0) {
            try (OutputStream responseBody = exchange.getResponseBody()) {
                responseBody.write(bodyAsBytes);
                responseBody.flush();
            }
        } else {
            exchange.getResponseBody().flush();
        }
    }

    private static AzureFunctionHttpRequest createHttpRequest(HttpExchange request, ApplicationContextProvider applicationContextProvider) {
        ApplicationContext applicationContext = applicationContextProvider.getApplicationContext();
        HttpRequestMessageBuilder<Object> requestMessageBuilder = HttpRequestMessageBuilder.builder(
            com.microsoft.azure.functions.HttpMethod.value(request.getRequestMethod()),
            request.getRequestURI().toString(),
            applicationContext
        );
        Set<String> headerNames = request.getRequestHeaders().keySet();
        for (String headerName : headerNames) {
            List<String> values = request.getRequestHeaders().get(headerName);
            requestMessageBuilder.header(headerName, String.join(",", values));
        }
        Map<String, List<String>> parameters = new QueryStringDecoder(request.getRequestURI()).parameters();
        parameters.keySet().forEach(parameterName -> {
            List<String> values = parameters.get(parameterName);
            requestMessageBuilder.parameter(parameterName, String.join(",", values));
        });

        HttpMethod httpMethod = HttpMethod.parse(request.getRequestMethod());
        if (HttpMethod.permitsRequestBody(httpMethod)) {
            try (InputStream inputStream = request.getRequestBody();
                 InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                 BufferedReader requestBody = new BufferedReader(inputStreamReader)) {
                String body = IOUtils.readText(requestBody);
                requestMessageBuilder.body(body);
            } catch (IOException e) {
                // ignore
            }
        }

        HttpRequestMessage<Optional<String>> requestMessage = requestMessageBuilder.buildEncoded();
        ConversionService handlerConversionService = applicationContext.getBean(ConversionService.class);
        BinaryTypeConfiguration binaryTypeConfiguration = applicationContext.getBean(BinaryTypeConfiguration.class);
        return new AzureFunctionHttpRequest<>(
            requestMessage,
            new AzureFunctionHttpResponse<>(
                requestMessage,
                handlerConversionService,
                binaryTypeConfiguration
            ),
            new DefaultExecutionContext(),
            handlerConversionService,
            binaryTypeConfiguration,
            applicationContext.getBean(BodyBuilder.class)
        );
    }

    /**
     * Default execution context impl. used for testing.
     */
    private static class DefaultExecutionContext implements ExecutionContext {

        @Override
        public Logger getLogger() {
            return LogManager.getLogManager().getLogger(AzureFunctionHttpHandler.class.getName());
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
