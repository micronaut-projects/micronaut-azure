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

import com.microsoft.azure.functions.HttpRequestMessage;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.context.ApplicationContext;
import io.micronaut.http.HttpMethod;

import java.net.URI;
import java.util.Objects;
import java.util.Optional;

/**
 * Utility interface to help with testing of Azure cloud functions.
 *
 * @param <T> The body type of the request
 * @since 1.0
 */
public interface HttpRequestMessageBuilder<T> {

    /**
     * Sets the request method.
     * @param method The request method
     * @return This builder
     */
    HttpRequestMessageBuilder<T> method(com.microsoft.azure.functions.HttpMethod method);

    /**
     * Sets the request URI.
     * @param uri The URI
     * @return This builder
     */
    HttpRequestMessageBuilder<T> uri(URI uri);

    /**
     * Sets a request header.
     * @param name The header
     * @param value The value
     * @return The message builder
     */
    HttpRequestMessageBuilder<T> header(String name, String value);

    /**
     * Sets a request query parameter.
     * @param name The name
     * @param value The value
     * @return The builder
     */
    HttpRequestMessageBuilder<T> parameter(String name, String value);

    /**
     * Sets the request body.
     * @param body The body
     * @param <B> The body type
     * @return This builder
     */
    <B> HttpRequestMessageBuilder<B> body(B body);

    /**
     * @return Builds the message
     */
    HttpRequestMessage<T> build();

    /**
     * @return Builds the message
     */
    HttpRequestMessage<Optional<String>> buildEncoded();

    /**
     * Sets the request method.
     * @param method The request method
     * @return This builder
     */
    default HttpRequestMessageBuilder<T> method(HttpMethod method) {
        return method(com.microsoft.azure.functions.HttpMethod.value(method.name()));
    }

    /**
     * Sets the request method.
     * @param method The request method
     * @return This builder
     */
    default HttpRequestMessageBuilder<T> method(String method) {
        return method(com.microsoft.azure.functions.HttpMethod.value(Objects.requireNonNull(method, "The method cannot be null")));
    }

    /**
     * Sets the request URI.
     * @param uri The URI
     * @return This builder
     */
    default HttpRequestMessageBuilder<T> uri(String uri) {
        return uri(URI.create(uri));
    }

    /**
     * Create a builder for the given application context.
     * @param <T> The builder body type
     * @param method The HTTP method
     * @param uri The URI
     * @param applicationContext The context
     * @return The builder
     */
    static <T> HttpRequestMessageBuilder<T> builder(com.microsoft.azure.functions.HttpMethod method, String uri, @NonNull ApplicationContext applicationContext) {
        return new DefaultHttpRequestMessageBuilder<>(
                Objects.requireNonNull(method, "method cannot be null"),
                URI.create(Objects.requireNonNull(uri, "URI cannot be null")),
                Objects.requireNonNull(applicationContext, "application context cannot be null")
        );
    }
}
