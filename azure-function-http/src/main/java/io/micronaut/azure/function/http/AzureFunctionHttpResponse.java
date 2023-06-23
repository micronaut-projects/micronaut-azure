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

import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatusType;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.core.convert.value.MutableConvertibleValuesMap;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.http.CaseInsensitiveMutableHttpHeaders;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpHeaders;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.netty.cookies.NettyCookie;
import io.micronaut.servlet.http.ServletHttpResponse;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
    private  final MutableHttpHeaders headers;
    private final BinaryContentConfiguration binaryContentConfiguration;
    private MutableConvertibleValues<Object> attributes;
    private B bodyObject;
    private String reason = HttpStatus.OK.getReason();

    public AzureFunctionHttpResponse(
        ConversionService conversionService,
        BinaryContentConfiguration binaryContentConfiguration
    ) {
        this.headers = new CaseInsensitiveMutableHttpHeaders(conversionService);
        this.binaryContentConfiguration = binaryContentConfiguration;
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
        if (cookie instanceof NettyCookie nettyCookie) {
            final String encoded = ServerCookieEncoder.STRICT.encode(nettyCookie.getNettyCookie());
            header(HttpHeaders.SET_COOKIE, encoded);
        }
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
        HttpResponseMessage.Builder responseBuilder = new ResponseBuilder().status(com.microsoft.azure.functions.HttpStatus.valueOf(status));
        getHeaders().forEach((s, strings) -> {
            for (String string : strings) {
                responseBuilder.header(s, string);
            }
        });
        if (bodyObject != null) {
            if (binaryContentConfiguration.isBinary(getHeaders().getContentType().orElse(null))) {
                responseBuilder.body(body.toByteArray());
            } else {
                responseBuilder.body(body.toString(getCharacterEncoding()));
            }
        }
        return responseBuilder.build();
    }

    private static class ResponseBuilder implements HttpResponseMessage.Builder, HttpResponseMessage, HttpHeaders {
        private HttpStatusType status = com.microsoft.azure.functions.HttpStatus.OK;
        private final Map<String, List<String>> headers = new LinkedHashMap<>(3);
        private Object body;

        @Override
        public Builder status(HttpStatusType status) {
            this.status = status;
            return this;
        }

        @Override
        public Builder header(String key, String value) {
            headers.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
            return this;
        }

        @Override
        public Builder body(Object body) {
            this.body = body;
            return this;
        }

        @Override
        public HttpResponseMessage build() {
            return this;
        }

        @Override
        public HttpStatusType getStatus() {
            return status;
        }

        @Override
        public String getHeader(String key) {
            List<String> v = headers.get(key);
            if (CollectionUtils.isNotEmpty(v)) {
                return v.iterator().next();
            }
            return null;
        }

        @Override
        public Object getBody() {
            return this.body;
        }

        @Override
        public List<String> getAll(CharSequence name) {
            List<String> values = headers.get(name.toString());
            if (values != null) {
                return Collections.unmodifiableList(values);
            }
            return Collections.emptyList();
        }

        @Override
        public String get(CharSequence name) {
            return getHeader(name.toString());
        }

        @Override
        public Set<String> names() {
            return headers.keySet();
        }

        @Override
        public Collection<List<String>> values() {
            return headers.values();
        }

        @Override
        public <T> Optional<T> get(CharSequence name, ArgumentConversionContext<T> conversionContext) {
            return ConversionService.SHARED
                .convert(name.toString(), conversionContext);
        }
    }
}
