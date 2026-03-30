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

import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.HttpStatusType;
import io.micronaut.context.ApplicationContext;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.MediaType;
import io.micronaut.http.body.MessageBodyHandlerRegistry;
import io.micronaut.http.body.MessageBodyWriter;
import io.micronaut.http.simple.SimpleHttpHeaders;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Internal class for building request messages.
 *
 * @param <T> The body type
 */
@Internal
class DefaultHttpRequestMessageBuilder<T> implements HttpRequestMessageBuilder<T>, HttpRequestMessage<T> {

    private final ApplicationContext applicationContext;
    private HttpMethod method = HttpMethod.GET;
    private URI uri;
    private final Map<String, String> headers = new LinkedHashMap<>(3);
    private final Map<String, String> queryParams = new LinkedHashMap<>(3);
    private Object body;
    private Optional<MessageBodyHandlerRegistry> messageBodyHandlerRegistry;

    public DefaultHttpRequestMessageBuilder(HttpMethod method, URI uri, ApplicationContext applicationContext) {
        method(method);
        uri(uri);
        this.applicationContext = applicationContext;
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
            headers.merge(headerName, value, (v1, v2) -> String.join(",", v1, v2));
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
    public HttpRequestMessage<Optional<String>> buildEncoded() {
        return buildEncodedRequest();
    }

    @Override
    public HttpResponseMessage invoke() {
        return applicationContext.getBean(AzureHttpFunction.class).route(
            buildEncodedRequest(),
            new DefaultExecutionContext()
        );
    }

    private HttpRequestMessage<Optional<String>> buildEncodedRequest() {
        if (this.body != null) {
            Object currentBody = this.body;
            if (currentBody instanceof Optional<?> optional) {
                this.body = optional;
            } else if (currentBody instanceof byte[] bytes) {
                this.body = Optional.of(bytes);
            } else if (currentBody instanceof CharSequence) {
                this.body = Optional.of(currentBody.toString());
            } else {
                MediaType mediaType = resolveContentType();
                if (isJsonLike(mediaType)) {
                    serializeWithMessageBodyWriter(mediaType, currentBody)
                        .ifPresentOrElse(serialized -> this.body = Optional.of(serialized),
                            () -> this.body = Optional.of(currentBody.toString()));
                } else {
                    this.body = Optional.of(currentBody.toString());
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

    private Optional<String> serializeWithMessageBodyWriter(MediaType mediaType, Object source) {
        Optional<MessageBodyHandlerRegistry> registryOptional = messageBodyHandlerRegistry();
        if (registryOptional.isEmpty()) {
            return Optional.empty();
        }
        @SuppressWarnings("unchecked")
        Argument<Object> argument = (Argument<Object>) Argument.of(source.getClass());
        Optional<MessageBodyWriter<Object>> writerOptional = registryOptional.get().findWriter(argument, mediaType);
        if (writerOptional.isEmpty()) {
            return Optional.empty();
        }
        MessageBodyWriter<Object> writer = writerOptional.get().createSpecific(argument);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            SimpleHttpHeaders headers = new SimpleHttpHeaders();
            writer.writeTo(argument, mediaType, source, headers, baos);
            Charset charset = MessageBodyWriter.findCharset(mediaType, headers)
                .orElseGet(() -> mediaType.getCharset().orElse(StandardCharsets.UTF_8));
            return Optional.of(new String(baos.toByteArray(), charset));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    private Optional<MessageBodyHandlerRegistry> messageBodyHandlerRegistry() {
        if (messageBodyHandlerRegistry == null) {
            messageBodyHandlerRegistry = applicationContext.findBean(MessageBodyHandlerRegistry.class);
        }
        return messageBodyHandlerRegistry;
    }

    private MediaType resolveContentType() {
        String contentType = getHeaders().get(HttpHeaders.CONTENT_TYPE);
        if (contentType == null) {
            return MediaType.APPLICATION_JSON_TYPE;
        }
        try {
            return new MediaType(contentType);
        } catch (IllegalArgumentException e) {
            return MediaType.APPLICATION_JSON_TYPE;
        }
    }

    private static boolean isJsonLike(MediaType mediaType) {
        if (mediaType == null) {
            return true;
        }
        if (MediaType.APPLICATION_JSON_TYPE.equals(mediaType)) {
            return true;
        }
        String subtype = mediaType.getSubtype();
        return subtype != null && subtype.endsWith("+json");
    }
}
