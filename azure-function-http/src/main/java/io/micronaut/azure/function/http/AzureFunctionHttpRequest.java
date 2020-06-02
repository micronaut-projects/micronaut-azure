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

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.ConvertibleValues;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.core.convert.value.MutableConvertibleValuesMap;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpParameters;
import io.micronaut.http.MediaType;
import io.micronaut.http.codec.CodecException;
import io.micronaut.http.codec.MediaTypeCodec;
import io.micronaut.http.codec.MediaTypeCodecRegistry;
import io.micronaut.http.cookie.Cookies;
import io.micronaut.servlet.http.ServletCookies;
import io.micronaut.servlet.http.ServletExchange;
import io.micronaut.servlet.http.ServletHttpRequest;
import io.micronaut.servlet.http.ServletHttpResponse;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of Micronaut's request interface for Azure.
 *
 * @param <B> The body type
 * @since 1.0
 * @author graemerocher
 */
@Internal
public class AzureFunctionHttpRequest<B>
        implements ServletHttpRequest<HttpRequestMessage<Optional<byte[]>>, B>,
        ServletExchange<HttpRequestMessage<Optional<byte[]>>, HttpResponseMessage> {
    private final HttpRequestMessage<Optional<byte[]>> azureRequest;
    private final URI uri;
    private final HttpMethod method;
    private final AzureMutableHeaders headers;
    private final MediaTypeCodecRegistry codecRegistry;
    private final AzureFunctionHttpResponse<?> azureResponse;
    private final ExecutionContext executionContext;
    private HttpParameters httpParameters;
    private MutableConvertibleValues<Object> attributes;
    private Object body;
    private ServletCookies cookies;

    /**
     * Default constructor.
     *
     * @param contextPath      The context path
     * @param azureRequest     The native google request
     * @param codecRegistry    The codec registry
     * @param executionContext The execution context.
     */
    AzureFunctionHttpRequest(
            String contextPath,
            HttpRequestMessage<Optional<byte[]>> azureRequest,
            MediaTypeCodecRegistry codecRegistry,
            ExecutionContext executionContext) {
        this.executionContext = executionContext;
        this.azureRequest = azureRequest;
        this.azureResponse = new AzureFunctionHttpResponse<>(azureRequest, codecRegistry);
        this.uri = azureRequest.getUri();
        HttpMethod method;
        try {
            method = HttpMethod.valueOf(azureRequest.getHttpMethod().name());
        } catch (IllegalArgumentException e) {
            method = HttpMethod.CUSTOM;
        }
        this.method = method;
        this.headers = new AzureMutableHeaders(azureRequest.getHeaders(), ConversionService.SHARED);
        this.codecRegistry = codecRegistry;
    }

    @Override
    public String getMethodName() {
        return azureRequest.getHttpMethod().name();
    }

    /**
     * @return The execution context.
     */
    public ExecutionContext getExecutionContext() {
        return executionContext;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(
                azureRequest.getBody().orElseThrow(() -> new IOException("Empty Body"))
        );
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(getInputStream(), getCharacterEncoding()));
    }

    @Override
    public HttpRequestMessage<Optional<byte[]>> getNativeRequest() {
        return azureRequest;
    }

    /**
     * Reference to the response object.
     *
     * @return The response.
     */
    AzureFunctionHttpResponse<?> getAzureResponse() {
        return azureResponse;
    }

    @Nonnull
    @Override
    public Cookies getCookies() {
        ServletCookies cookies = this.cookies;
        if (cookies == null) {
            synchronized (this) { // double check
                cookies = this.cookies;
                if (cookies == null) {
                    cookies = new ServletCookies(getPath(), getHeaders(), ConversionService.SHARED);
                    this.cookies = cookies;
                }
            }
        }
        return cookies;
    }

    @Nonnull
    @Override
    public HttpParameters getParameters() {
        HttpParameters httpParameters = this.httpParameters;
        if (httpParameters == null) {
            synchronized (this) { // double check
                httpParameters = this.httpParameters;
                if (httpParameters == null) {
                    httpParameters = new AzureParameters();
                    this.httpParameters = httpParameters;
                }
            }
        }
        return httpParameters;
    }

    @Nonnull
    @Override
    public HttpMethod getMethod() {
        return method;
    }

    @Nonnull
    @Override
    public URI getUri() {
        return this.uri;
    }

    @Nonnull
    @Override
    public HttpHeaders getHeaders() {
        return headers;
    }

    @Nonnull
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

    @Nonnull
    @Override
    public Optional<B> getBody() {
        return (Optional<B>) getBody(Argument.STRING);
    }

    @Nonnull
    @Override
    public <T> Optional<T> getBody(@Nonnull Argument<T> arg) {
        if (arg != null) {
            final Class<T> type = arg.getType();
            final MediaType contentType = getContentType().orElse(MediaType.APPLICATION_JSON_TYPE);
            if (body == null) {

                if (isFormSubmission(contentType)) {
                    body = getParameters();
                    if (ConvertibleValues.class == type) {
                        return (Optional<T>) Optional.of(body);
                    } else {
                        return Optional.empty();
                    }
                } else {

                    final MediaTypeCodec codec = codecRegistry.findCodec(contentType, type).orElse(null);
                    if (codec != null) {
                        try (InputStream inputStream = getInputStream()) {
                            if (ConvertibleValues.class == type) {
                                final Map map = codec.decode(Map.class, inputStream);
                                body = ConvertibleValues.of(map);
                                return (Optional<T>) Optional.of(body);
                            } else {
                                final T value = codec.decode(arg, inputStream);
                                body = value;
                                return Optional.ofNullable(value);
                            }
                        } catch (IOException e) {
                            throw new CodecException("Error decoding request body: " + e.getMessage(), e);
                        }

                    }
                }
            } else {
                if (type.isInstance(body)) {
                    return (Optional<T>) Optional.of(body);
                } else {
                    if (body != httpParameters) {
                        final T result = ConversionService.SHARED.convertRequired(body, arg);
                        return Optional.ofNullable(result);
                    }
                }

            }
        }
        return Optional.empty();
    }

    private boolean isFormSubmission(MediaType contentType) {
        return MediaType.MULTIPART_FORM_DATA_TYPE.equals(contentType) || MediaType.MULTIPART_FORM_DATA_TYPE.equals(contentType);
    }

    @Override
    public ServletHttpRequest<HttpRequestMessage<Optional<byte[]>>, ? super Object> getRequest() {
        //noinspection unchecked
        return (ServletHttpRequest) this;
    }

    @Override
    public ServletHttpResponse<HttpResponseMessage, ? super Object> getResponse() {
        //noinspection unchecked
        return (ServletHttpResponse<HttpResponseMessage, ? super Object>) azureResponse;
    }

    /**
     * Models the http parameters.
     */
    private final class AzureParameters implements HttpParameters {
        @Override
        public Optional<String> getFirst(CharSequence name) {
            ArgumentUtils.requireNonNull("name", name);
            return Optional.ofNullable(azureRequest.getQueryParameters().get(name.toString()));
        }

        @Override
        public List<String> getAll(CharSequence name) {
            if (name != null) {
                String v = azureRequest.getQueryParameters().get(name.toString());
                if (v != null) {
                    return Collections.singletonList(v);
                }
            }
            return Collections.emptyList();
        }

        @Nullable
        @Override
        public String get(CharSequence name) {
            return getFirst(name).orElse(null);
        }

        @Override
        public Set<String> names() {
            return azureRequest.getQueryParameters().keySet();
        }

        @Override
        public Collection<List<String>> values() {
            return azureRequest.getQueryParameters()
                    .values()
                    .stream().map(Collections::singletonList)
                    .collect(Collectors.toList());
        }

        @Override
        public <T> Optional<T> get(CharSequence name, ArgumentConversionContext<T> conversionContext) {
            if (name != null) {
                String value = azureRequest.getQueryParameters().get(name.toString());
                if (value != null) {
                    if (conversionContext.getArgument().getType().isInstance(value)) {
                        return (Optional<T>) Optional.of(value);
                    } else {
                        return ConversionService.SHARED.convert(value, conversionContext);
                    }
                }
            }
            return Optional.empty();
        }
    }
}
