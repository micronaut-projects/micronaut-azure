package io.micronaut.azure.function.http

import com.microsoft.azure.functions.HttpMethod
import com.microsoft.azure.functions.HttpRequestMessage
import com.microsoft.azure.functions.HttpResponseMessage
import com.microsoft.azure.functions.HttpStatus
import com.microsoft.azure.functions.HttpStatusType

import java.nio.charset.StandardCharsets

class MockAzureRequest implements HttpRequestMessage<Optional<byte[]>> {
    final HttpMethod httpMethod
    final URI uri
    final byte[] rawBody
    final Map<String, String> headers = [:]
    final Map<String, String> queryParameters = [:]

    MockAzureRequest(HttpMethod httpMethod, String uri) {
        this.httpMethod = httpMethod
        this.uri = URI.create(uri)
        this.rawBody = null;
    }

    MockAzureRequest(HttpMethod httpMethod, String uri, byte[] body) {
        this.httpMethod = httpMethod
        this.uri = URI.create(uri)
        this.rawBody = body
    }

    @Override
    Optional<byte[]> getBody() {
        return Optional.ofNullable(body)
    }

    @Override
    HttpResponseMessage.Builder createResponseBuilder(HttpStatus status) {
        return new Builder().status(status)
    }

    @Override
    HttpResponseMessage.Builder createResponseBuilder(HttpStatusType status) {
        return new Builder().status(status)
    }

    static class Builder implements HttpResponseMessage.Builder, HttpResponseMessage {
        HttpStatusType status = HttpStatus.OK
        final Map<String, String> headers = [:]
        Object body

        @Override
        HttpResponseMessage.Builder status(HttpStatusType status) {
            this.status = status
            return this
        }

        @Override
        HttpResponseMessage.Builder header(String key, String value) {
            headers.put(key, value)
            return this
        }

        @Override
        HttpResponseMessage.Builder body(Object body) {
            this.body = body
            return this
        }

        @Override
        HttpResponseMessage build() {
            return this
        }

        @Override
        String getHeader(String key) {
            return headers.get(key)
        }

        @Override
        Object getBody() {
            return this.body
        }

        String getBodyAsString() {
            if (this.body instanceof byte[]) {
                return new String((byte[])this.body, StandardCharsets.UTF_8)
            } else {
                return this.body?.toString()
            }
        }
    }
}
