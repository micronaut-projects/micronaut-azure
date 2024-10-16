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
package io.micronaut.azure.function.http;

import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.core.convert.value.MutableConvertibleValuesMap;
import io.micronaut.core.util.StringUtils;
import io.micronaut.function.BinaryTypeConfiguration;
import io.micronaut.http.CaseInsensitiveMutableHttpHeaders;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpHeaders;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.cookie.ServerCookieEncoder;
import io.micronaut.servlet.http.ServletHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Optional;

/**
 * Azure implementation of {@link ServletHttpResponse}.
 *
 * @author Tim Yates
 * @since 5.0.0
 * @param <B> Body Type
 */
@Internal
public final class AzureFunctionHttpResponse<B> implements ServletHttpResponse<HttpResponseMessage, B> {

    private static final Logger LOG = LoggerFactory.getLogger(AzureFunctionHttpResponse.class);

    private final ByteArrayOutputStream body = new ByteArrayOutputStream();
    private int status = HttpStatus.OK.getCode();
    private final HttpRequestMessage<Optional<String>> azureRequest;
    private  final MutableHttpHeaders headers;
    private final BinaryTypeConfiguration binaryTypeConfiguration;
    private MutableConvertibleValues<Object> attributes;
    private B bodyObject;
    private String reason = HttpStatus.OK.getReason();

    public AzureFunctionHttpResponse(
        HttpRequestMessage<Optional<String>> azureRequest,
        ConversionService conversionService,
        BinaryTypeConfiguration binaryTypeConfiguration
    ) {
        this.azureRequest = azureRequest;
        this.headers = new CaseInsensitiveMutableHttpHeaders(conversionService);
        this.binaryTypeConfiguration = binaryTypeConfiguration;
    }

    @Override
    public OutputStream getOutputStream() {
        return body;
    }

    @Override
    public BufferedWriter getWriter() {
        return new BufferedWriter(new OutputStreamWriter(body, getCharacterEncoding()));
    }

    @Override
    public MutableHttpResponse<B> cookie(Cookie cookie) {
        ServerCookieEncoder.INSTANCE.encode(cookie).forEach(c -> header(HttpHeaders.SET_COOKIE, c));
        return this;
    }

    @Override
    public MutableHttpHeaders getHeaders() {
        return headers;
    }

    @Override
    public MutableConvertibleValues<Object> getAttributes() {
        MutableConvertibleValues<Object> localAttributes = this.attributes;
        if (localAttributes == null) {
            synchronized (this) { // double check
                localAttributes = this.attributes;
                if (localAttributes == null) {
                    localAttributes = new MutableConvertibleValuesMap<>();
                    this.attributes = localAttributes;
                }
            }
        }
        return localAttributes;
    }

    @Override
    public Optional<B> getBody() {
        return Optional.ofNullable(bodyObject);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> MutableHttpResponse<T> body(@Nullable T body) {
        this.bodyObject = (B) body;
        return (MutableHttpResponse<T>) this;
    }

    @Override
    public MutableHttpResponse<B> status(int status, CharSequence message) {
        this.status = status;
        if (message == null) {
            this.reason = HttpStatus.getDefaultReason(status);
        } else {
            this.reason = message.toString();
        }
        return this;
    }

    @Override
    public int code() {
        return status;
    }

    @Override
    public String reason() {
        return reason;
    }

    @Override
    public HttpResponseMessage getNativeResponse() {
        LOG.trace("Creating Azure Function HTTP Response");

        if (this.bodyObject instanceof HttpResponseMessage.Builder builder) {
            LOG.trace("Using Azure Function HTTP Response Builder");
            return builder.build();
        }
        HttpResponseMessage.Builder responseBuilder = azureRequest.createResponseBuilder(
            com.microsoft.azure.functions.HttpStatus.valueOf(status)
        );
        getHeaders().forEach((s, strings) -> {
            for (String string : strings) {
                responseBuilder.header(s, string);
            }
        });
        if (binaryTypeConfiguration.isMediaTypeBinary(getHeaders().getContentType().orElse(null))) {
            responseBuilder.body(body.toByteArray());
        } else {
            String bodyStr = body.toString(getCharacterEncoding());
            if (StringUtils.isNotEmpty(bodyStr)) {
                responseBuilder.body(bodyStr);
            }
        }
        return responseBuilder.build();
    }
}
