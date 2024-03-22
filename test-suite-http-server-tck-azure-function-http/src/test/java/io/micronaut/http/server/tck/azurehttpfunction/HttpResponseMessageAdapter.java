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
package io.micronaut.http.server.tck.azurehttpfunction;

import com.microsoft.azure.functions.HttpResponseMessage;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.core.convert.value.MutableConvertibleValuesMap;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.http.CaseInsensitiveMutableHttpHeaders;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpHeaders;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.json.JsonMapper;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adapter for {@link HttpResponseMessage} to {@link MutableHttpResponse}.
 *
 * @param <T> The body type
 * @since 5.0.0
 */
@Internal
public class HttpResponseMessageAdapter<T> implements MutableHttpResponse<T> {

    private HttpResponseMessage message;
    private final MutableConvertibleValues<Object> attributes = new MutableConvertibleValuesMap<>();
    private final MutableHttpHeaders headers;
    private Map<String, Cookie> cookies = new ConcurrentHashMap<>(2);
    private final JsonMapper jsonMapper;

    private Integer status;
    private String reason;

    public HttpResponseMessageAdapter(
        HttpResponseMessage message,
        ConversionService conversionService,
        JsonMapper jsonMapper,
        Set<String> extraHeaders
    ) {
        this.message = message;
        this.headers = new CaseInsensitiveMutableHttpHeaders(conversionService);
        this.jsonMapper = jsonMapper;
        populateHeaders(HttpHeaders.STANDARD_HEADERS);
        populateHeaders(extraHeaders);
    }

    private void populateHeaders(Collection<String> standardHeaders) {
        for (String header : standardHeaders) {
            String value = message.getHeader(header);
            if (value != null) {
                headers.add(header, value);
            }
        }
    }

    @Override
    public MutableHttpResponse<T> cookie(Cookie cookie) {
        this.cookies.put(cookie.getName(), cookie);
        return this;
    }

    @Override
    public MutableHttpHeaders getHeaders() {
        return headers;
    }

    @Override
    public MutableConvertibleValues<Object> getAttributes() {
        return attributes;
    }

    @Override
    public Optional<T> getBody() {
        return (Optional<T>) Optional.ofNullable(message.getBody());
    }

    @Override
    public <B> MutableHttpResponse<B> body(B body) {
        return (MutableHttpResponse<B>) this;
    }

    @Override
    public <T> @NonNull Optional<T> getBody(@NonNull ArgumentConversionContext<T> conversionContext) {
        ArgumentUtils.requireNonNull("conversionContext", conversionContext);
        return Optional
            .ofNullable(message.getBody())
            .flatMap((b) -> ConversionService.SHARED.convert(b, conversionContext))
            .or(() -> headers.contentType()
                .filter(m -> m.matches(MediaType.APPLICATION_JSON_TYPE))
                .flatMap(m -> fromJson((String) message.getBody(), conversionContext.getArgument()))
            );
    }

    private <T> Optional<T> fromJson(String body, Argument<T> argument) {
        try {
            return Optional.of(jsonMapper.readValue(body, argument));
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    @Override
    public MutableHttpResponse<T> status(int status, CharSequence message) {
        ArgumentUtils.requireNonNull("status", status);
        if (message == null) {
            this.reason = HttpStatus.getDefaultReason(status);
        } else {
            this.reason = message.toString();
        }
        this.status = status;
        return this;
    }

    @Override
    public int code() {
        if (status != null) {
            return status;
        }
        return getStatus().getCode();
    }

    @Override
    public String reason() {
        if (reason != null) {
            return reason;
        }
        return getStatus().getReason();
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.valueOf(status == null ? message.getStatusCode() : status);
    }
}
