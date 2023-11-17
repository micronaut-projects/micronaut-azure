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
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.MediaType;
import io.micronaut.http.codec.MediaTypeCodec;
import io.micronaut.http.codec.MediaTypeCodecRegistry;

import java.net.URI;
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
            if (this.body instanceof byte[] byteArr) {
                this.body = Optional.of(byteArr);
            } else if (this.body instanceof CharSequence) {
                this.body = Optional.of(this.body.toString());
            } else {
                MediaTypeCodecRegistry codecRegistry = applicationContext.getBean(MediaTypeCodecRegistry.class);
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
