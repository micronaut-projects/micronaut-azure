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
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.core.convert.value.MutableConvertibleValuesMap;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.http.CaseInsensitiveMutableHttpHeaders;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpHeaders;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.cookie.Cookie;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @param <T> The body type
 */
@Internal
public class HttpResponseMessageAdapter<T> implements MutableHttpResponse<T> {

    private HttpResponseMessage message;
    private final ConversionService conversionService;
    private final Set<String> extraHeaders;
    private final MutableConvertibleValues<Object> attributes = new MutableConvertibleValuesMap<>();
    private final MutableHttpHeaders headers;
    private Map<String, Cookie> cookies = new ConcurrentHashMap<>(2);

    private Integer status;
    private String reason;

    public HttpResponseMessageAdapter(
        HttpResponseMessage message,
        ConversionService conversionService,
        Set<String> extraHeaders
    ) {
        this.message = message;
        this.conversionService = conversionService;
        this.headers = new CaseInsensitiveMutableHttpHeaders(conversionService);
        this.extraHeaders = extraHeaders;
        populateHeaders(STANDARD_HEADERS);
        populateHeaders(extraHeaders);
    }

    private void populateHeaders(Set<String> standardHeaders) {
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

    private final static Set<String> STANDARD_HEADERS = Set.of(
        HttpHeaders.ACCEPT,
        HttpHeaders.ACCEPT_CH,
        HttpHeaders.ACCEPT_CH_LIFETIME,
        HttpHeaders.ACCEPT_CHARSET,
        HttpHeaders.ACCEPT_ENCODING,
        HttpHeaders.ACCEPT_LANGUAGE,
        HttpHeaders.ACCEPT_RANGES,
        HttpHeaders.ACCEPT_PATCH,
        HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS,
        HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
        HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,
        HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
        HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS,
        HttpHeaders.ACCESS_CONTROL_MAX_AGE,
        HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS,
        HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD,
        HttpHeaders.AGE,
        HttpHeaders.ALLOW,
        HttpHeaders.AUTHORIZATION,
        HttpHeaders.AUTHORIZATION_INFO,
        HttpHeaders.CACHE_CONTROL,
        HttpHeaders.CONNECTION,
        HttpHeaders.CONTENT_BASE,
        HttpHeaders.CONTENT_DISPOSITION,
        HttpHeaders.CONTENT_DPR,
        HttpHeaders.CONTENT_ENCODING,
        HttpHeaders.CONTENT_LANGUAGE,
        HttpHeaders.CONTENT_LENGTH,
        HttpHeaders.CONTENT_LOCATION,
        HttpHeaders.CONTENT_TRANSFER_ENCODING,
        HttpHeaders.CONTENT_MD5,
        HttpHeaders.CONTENT_RANGE,
        HttpHeaders.CONTENT_TYPE,
        HttpHeaders.COOKIE,
        HttpHeaders.CROSS_ORIGIN_RESOURCE_POLICY,
        HttpHeaders.DATE,
        HttpHeaders.DEVICE_MEMORY,
        HttpHeaders.DOWNLINK,
        HttpHeaders.DPR,
        HttpHeaders.ECT,
        HttpHeaders.ETAG,
        HttpHeaders.EXPECT,
        HttpHeaders.EXPIRES,
        HttpHeaders.FEATURE_POLICY,
        HttpHeaders.FORWARDED,
        HttpHeaders.FROM,
        HttpHeaders.HOST,
        HttpHeaders.IF_MATCH,
        HttpHeaders.IF_MODIFIED_SINCE,
        HttpHeaders.IF_NONE_MATCH,
        HttpHeaders.IF_RANGE,
        HttpHeaders.IF_UNMODIFIED_SINCE,
        HttpHeaders.LAST_MODIFIED,
        HttpHeaders.LINK,
        HttpHeaders.LOCATION,
        HttpHeaders.MAX_FORWARDS,
        HttpHeaders.ORIGIN,
        HttpHeaders.PRAGMA,
        HttpHeaders.PROXY_AUTHENTICATE,
        HttpHeaders.PROXY_AUTHORIZATION,
        HttpHeaders.RANGE,
        HttpHeaders.REFERER,
        HttpHeaders.REFERRER_POLICY,
        HttpHeaders.RETRY_AFTER,
        HttpHeaders.RTT,
        HttpHeaders.SAVE_DATA,
        HttpHeaders.SEC_WEBSOCKET_KEY1,
        HttpHeaders.SEC_WEBSOCKET_KEY2,
        HttpHeaders.SEC_WEBSOCKET_LOCATION,
        HttpHeaders.SEC_WEBSOCKET_ORIGIN,
        HttpHeaders.SEC_WEBSOCKET_PROTOCOL,
        HttpHeaders.SEC_WEBSOCKET_VERSION,
        HttpHeaders.SEC_WEBSOCKET_KEY,
        HttpHeaders.SEC_WEBSOCKET_ACCEPT,
        HttpHeaders.SERVER,
        HttpHeaders.SET_COOKIE,
        HttpHeaders.SET_COOKIE2,
        HttpHeaders.SOURCE_MAP,
        HttpHeaders.TE,
        HttpHeaders.TRAILER,
        HttpHeaders.TRANSFER_ENCODING,
        HttpHeaders.UPGRADE,
        HttpHeaders.USER_AGENT,
        HttpHeaders.VARY,
        HttpHeaders.VIA,
        HttpHeaders.VIEWPORT_WIDTH,
        HttpHeaders.WARNING,
        HttpHeaders.WEBSOCKET_LOCATION,
        HttpHeaders.WEBSOCKET_ORIGIN,
        HttpHeaders.WEBSOCKET_PROTOCOL,
        HttpHeaders.WIDTH,
        HttpHeaders.WWW_AUTHENTICATE,
        HttpHeaders.X_AUTH_TOKEN
    );
}
