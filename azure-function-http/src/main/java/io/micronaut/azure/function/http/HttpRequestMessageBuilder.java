package io.micronaut.azure.function.http;

import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import io.micronaut.http.HttpMethod;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

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
     * @return Invokes the function
     */
    AzureHttpResponseMessage invoke();

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
     * An azure response message.
     */
    interface AzureHttpResponseMessage extends HttpResponseMessage {
        /**
         * @return The body as a string.
         */
        default String getBodyAsString() {
            Object body = getBody();
            if (body instanceof byte[]) {
                return new String((byte[]) body, StandardCharsets.UTF_8);
            } else if (body != null) {
                return body.toString();
            }
            return null;
        }
    }
}
