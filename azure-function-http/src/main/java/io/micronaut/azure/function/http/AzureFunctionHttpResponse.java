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
import com.microsoft.azure.functions.HttpResponseMessage;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.core.convert.value.MutableConvertibleValuesMap;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.http.*;
import io.micronaut.http.codec.MediaTypeCodec;
import io.micronaut.http.codec.MediaTypeCodecRegistry;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.netty.cookies.NettyCookie;
import io.micronaut.servlet.http.ServletHttpResponse;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Optional;

/**
 * Azure implementation of {@link ServletHttpResponse}.
 *
 * @author graemerocher
 * @since 1.0.0
 * @param <B> The body type
 */
@Internal
public class AzureFunctionHttpResponse<B> implements ServletHttpResponse<HttpResponseMessage, B> {

    private final MediaTypeCodecRegistry mediaTypeCodecRegistry;
    private final HttpRequestMessage<Optional<String>> azureRequest;
    private MutableConvertibleValues<Object> attributes;
    private B body;
    private HttpStatus status = HttpStatus.OK;
    private ByteArrayOutputStream outputStream;
    private MutableHttpHeaders headers = new AzureMutableHeaders(new LinkedHashMap<>(5), ConversionService.SHARED);

    /**
     * Default constructor.
     *
     * @param azureRequest               The Azure request object
     * @param mediaTypeCodecRegistry The media type codec registry
     */
    AzureFunctionHttpResponse(
            HttpRequestMessage<Optional<String>> azureRequest,
            MediaTypeCodecRegistry mediaTypeCodecRegistry) {
        this.azureRequest = azureRequest;
        this.mediaTypeCodecRegistry = mediaTypeCodecRegistry;
    }

    @Override
    public OutputStream getOutputStream() {
        ByteArrayOutputStream outputStream = this.outputStream;
        if (outputStream == null) {
            synchronized (this) { // double check
                outputStream = this.outputStream;
                if (outputStream == null) {
                    outputStream = new ByteArrayOutputStream();
                    this.outputStream = outputStream;
                }
            }
        }
        return outputStream;
    }

    @Override
    public BufferedWriter getWriter() {
        return new BufferedWriter(new OutputStreamWriter(getOutputStream(), getCharacterEncoding()));
    }

    @Override
    public MutableHttpResponse<B> cookie(Cookie cookie) {
        if (cookie instanceof NettyCookie) {
            NettyCookie nettyCookie = (NettyCookie) cookie;
            final String encoded = ServerCookieEncoder.STRICT.encode(nettyCookie.getNettyCookie());
            header(HttpHeaders.SET_COOKIE, encoded);
        }
        return this;
    }

    @Override
    public <T> MutableHttpResponse<T> body(@Nullable T body) {
        if (body instanceof CharSequence) {
            if (!getContentType().isPresent()) {
                contentType(MediaType.TEXT_PLAIN_TYPE);
            }
        }
        this.body = (B) body;
        return (MutableHttpResponse<T>) this;
    }

    @Override
    public MutableHttpHeaders getHeaders() {
        return this.headers;
    }

    @NonNull
    @Override
    public MutableConvertibleValues<Object> getAttributes() {
        MutableConvertibleValues<Object> attributes = this.attributes;
        if (attributes == null) {
            synchronized (this) { // double check
                attributes = this.attributes;
                if (attributes == null) {
                    attributes = new MutableConvertibleValuesMap<>();
                    this.attributes = attributes;
                }
            }
        }
        return attributes;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    @Override
    public Optional<B> getBody() {
        if (outputStream != null) {
            return (Optional<B>) Optional.of(outputStream.toByteArray());
        } else {
            return Optional.ofNullable(this.body);
        }
    }

    @Override
    public MutableHttpResponse<B> status(HttpStatus status, CharSequence message) {
        ArgumentUtils.requireNonNull("status", status);
        this.status = status;
        return this;
    }

    @Override
    public HttpStatus getStatus() {
        return this.status;
    }

    @Override
    public HttpResponseMessage getNativeResponse() {
        if (this.body instanceof HttpResponseMessage.Builder) {
            return ((HttpResponseMessage.Builder) this.body).build();
        } else {

            HttpResponseMessage.Builder responseBuilder = azureRequest.createResponseBuilder(
                    com.microsoft.azure.functions.HttpStatus.valueOf(status.getCode())
            );
            getHeaders().forEach((s, strings) -> {
                for (String string : strings) {
                    responseBuilder.header(s, string);
                }
            });
            getBody().ifPresent(b -> {
                if (b instanceof byte[]) {
                    responseBuilder.body(b);
                } else {
                    MediaTypeCodec codec = mediaTypeCodecRegistry
                            .findCodec(getContentType().orElse(MediaType.APPLICATION_JSON_TYPE), b.getClass())
                            .orElse(null);
                    if (codec != null) {
                        responseBuilder.body(codec.encode(b));
                    } else {
                        responseBuilder.body(b);
                    }
                }
            });
            return responseBuilder.build();
        }
    }

}

