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

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.core.convert.value.MutableConvertibleValuesMap;
import io.micronaut.core.execution.ExecutionFlow;
import io.micronaut.core.io.buffer.ByteBuffer;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.core.util.SupplierUtil;
import io.micronaut.http.CaseInsensitiveMutableHttpHeaders;
import io.micronaut.http.FullHttpRequest;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpHeaders;
import io.micronaut.http.MutableHttpParameters;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.cookie.Cookies;
import io.micronaut.http.simple.SimpleHttpParameters;
import io.micronaut.servlet.http.BodyBuilder;
import io.micronaut.servlet.http.ByteArrayByteBuffer;
import io.micronaut.servlet.http.MutableServletHttpRequest;
import io.micronaut.servlet.http.ParsedBodyHolder;
import io.micronaut.servlet.http.ServletExchange;
import io.micronaut.servlet.http.ServletHttpRequest;
import io.micronaut.servlet.http.ServletHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Servlet request implementation for Azure Functions.
 *
 * @param <T> The body type
 */
@Internal
@SuppressWarnings("java:S119") // More descriptive generics are better here
public final class AzureFunctionHttpRequest<T> implements
    MutableServletHttpRequest<HttpRequestMessage<Optional<String>>, T>,
    ServletExchange<HttpRequestMessage<Optional<String>>, HttpResponseMessage>,
    FullHttpRequest<T>,
    ParsedBodyHolder<T> {

    private static final Logger LOG = LoggerFactory.getLogger(AzureFunctionHttpRequest.class);

    private final ExecutionContext executionContext;
    private final BinaryContentConfiguration binaryContentConfiguration;
    private ConversionService conversionService;
    private final HttpRequestMessage<Optional<String>> requestEvent;
    private final AzureFunctionHttpResponse<Object> response;
    private URI uri;
    private final HttpMethod httpMethod;
    private Cookies cookies;
    private MutableConvertibleValues<Object> attributes;
    private Supplier<Optional<T>> body;
    private T parsedBody;
    private T overriddenBody;

    private ByteArrayByteBuffer<T> servletByteBuffer;

    public AzureFunctionHttpRequest(
        HttpRequestMessage<Optional<String>> request,
        AzureFunctionHttpResponse<Object> response,
        ExecutionContext executionContext,
        ConversionService conversionService,
        BinaryContentConfiguration binaryContentConfiguration,
        BodyBuilder bodyBuilder
    ) {
        this.executionContext = executionContext;
        this.conversionService = conversionService;
        this.binaryContentConfiguration = binaryContentConfiguration;
        this.requestEvent = request;
        this.response = response;
        this.uri = URI.create(requestEvent.getUri().getPath());
        this.httpMethod = parseMethod(requestEvent.getHttpMethod()::name);
        this.body = SupplierUtil.memoizedNonEmpty(() -> {
            T built = parsedBody != null ? parsedBody :  (T) bodyBuilder.buildBody(this::getInputStream, this);
            return Optional.ofNullable(built);
        });
    }

    public ExecutionContext getExecutionContext() {
        return executionContext;
    }

    public byte[] getBodyBytes() throws IOException {
        if (requestEvent.getBody().isPresent()) {
            return getBodyBytes(requestEvent.getBody()::get, () -> binaryContentConfiguration.isBinary(requestEvent.getHeaders().get(HttpHeaders.CONTENT_TYPE)));
        } else {
            return ArrayUtils.EMPTY_BYTE_ARRAY;
        }
    }

    protected static HttpMethod parseMethod(Supplier<String> httpMethodConsumer) {
        try {
            return HttpMethod.valueOf(httpMethodConsumer.get());
        } catch (IllegalArgumentException e) {
            return HttpMethod.CUSTOM;
        }
    }

    @Override
    public MutableHttpHeaders getHeaders() {
        Map<String, List<String>> headersMap = transformCommaSeparatedValue(requestEvent.getHeaders());
        return new CaseInsensitiveMutableHttpHeaders(headersMap, conversionService);
    }

    @Override
    public MutableHttpParameters getParameters() {
        MediaType mediaType = getContentType().orElse(MediaType.APPLICATION_JSON_TYPE);
        Map<CharSequence, List<String>> values = new HashMap<>(transformCommaSeparatedValue(requestEvent.getQueryParameters()));
        if (isFormSubmission(mediaType)) {
            Map<String, List<String>> parameters = null;
            try {
                parameters = new QueryStringDecoder(new String(getBodyBytes(), getCharacterEncoding()), false).parameters();
            } catch (IOException ex) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Error decoding form data: " + ex.getMessage(), ex);
                }
                parameters = new HashMap<>();
            }
            values.putAll(parameters);
        }
        return new SimpleHttpParameters(values, conversionService);
    }

    @Override
    public ServletHttpResponse<HttpResponseMessage, ?> getResponse() {
        return response;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return servletByteBuffer != null ? servletByteBuffer.toInputStream() : new ByteArrayInputStream(getBodyBytes());
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ServletHttpRequest<HttpRequestMessage<Optional<String>>, ? super Object> getRequest() {
        return (ServletHttpRequest) this;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream(), getCharacterEncoding()));
    }

    @Override
    public HttpRequestMessage<Optional<String>> getNativeRequest() {
        return requestEvent;
    }

    @Override
    public HttpMethod getMethod() {
        return httpMethod;
    }

    @Override
    public URI getUri() {
        return uri;
    }

    @NonNull
    @Override
    public Cookies getCookies() {
        Cookies localCookies = this.cookies;
        if (localCookies == null) {
            synchronized (this) { // double check
                localCookies = this.cookies;
                if (localCookies == null) {
                    localCookies = new AzureCookies(getPath(), getHeaders(), conversionService);
                    this.cookies = localCookies;
                }
            }
        }
        return localCookies;
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

    @NonNull
    @Override
    public Optional<T> getBody() {
        if (overriddenBody != null) {
            return Optional.of(overriddenBody);
        }
        return this.body.get();
    }

    @NonNull
    @Override
    public <B> Optional<B> getBody(Argument<B> arg) {
        return getBody().map(t -> conversionService.convertRequired(t, arg));
    }

    /**
     *
     * @param contentType Content Type
     * @return returns true if the content type is either application/x-www-form-urlencoded or multipart/form-data
     */
    protected boolean isFormSubmission(MediaType contentType) {
        return MediaType.APPLICATION_FORM_URLENCODED_TYPE.equals(contentType) || MediaType.MULTIPART_FORM_DATA_TYPE.equals(contentType);
    }

    @Override
    public MutableHttpRequest<T> cookie(Cookie cookie) {
        return this;
    }

    @Override
    public MutableHttpRequest<T> uri(URI uri) {
        this.uri = uri;
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <B> MutableHttpRequest<B> body(B body) {
        this.overriddenBody = (T) body;
        return (MutableHttpRequest<B>) this;
    }

    @Override
    public void setConversionService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public void setParsedBody(T body) {
        this.parsedBody = body;
    }

    @Override
    public @Nullable ByteBuffer<?> contents() {
        try {
            if (servletByteBuffer == null) {
                this.servletByteBuffer = new ByteArrayByteBuffer<>(getInputStream().readAllBytes());
            }
            return servletByteBuffer;
        } catch (IOException e) {
            throw new IllegalStateException("Error getting all body contents", e);
        }
    }

    @Override
    public @Nullable ExecutionFlow<ByteBuffer<?>> bufferContents() {
        return ExecutionFlow.just(contents());
    }

    /**
     *
     * @param bodySupplier HTTP Request's Body Supplier
     * @param base64EncodedSupplier Whether the body is Base 64 encoded
     * @return body bytes
     * @throws IOException if the body is empty
     */
    protected byte[] getBodyBytes(@NonNull Supplier<String> bodySupplier, @NonNull BooleanSupplier base64EncodedSupplier) throws IOException {
        String requestBody = bodySupplier.get();
        if (StringUtils.isEmpty(requestBody)) {
            throw new IOException("Empty Body");
        }
        return base64EncodedSupplier.getAsBoolean() ?
            Base64.getDecoder().decode(requestBody) : requestBody.getBytes(getCharacterEncoding());
    }

    @NonNull
    private static List<String> splitCommaSeparatedValue(@Nullable String value) {
        if (value == null) {
            return Collections.emptyList();
        }
        String[] arr = value.split(",");
        List<String> result = new ArrayList<>();
        for (String str : arr) {
            result.add(str.trim());
        }
        return result;
    }

    @NonNull
    private static Map<String, List<String>> transformCommaSeparatedValue(@Nullable Map<String, String> input) {
        if (input == null) {
            return Collections.emptyMap();
        }
        Map<String, List<String>> output = new HashMap<>();
        for (var entry: input.entrySet()) {
            output.put(entry.getKey(), splitCommaSeparatedValue(entry.getValue()));
        }
        return output;
    }
}
