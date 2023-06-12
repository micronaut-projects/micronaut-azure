package io.micronaut.http.server.tck.azurehttpfunction;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import io.micronaut.azure.function.AzureFunction;
import io.micronaut.azure.function.http.HttpRequestMessageBuilder;
import io.micronaut.context.ApplicationContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.http.simple.SimpleHttpResponseFactory;
import io.micronaut.http.tck.ServerUnderTest;
import io.micronaut.json.JsonMapper;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("java:S2187") // Suppress because despite its name, this is not a Test
public class AzureFunctionHttpServerUnderTest implements ServerUnderTest {

    Function function;

    public AzureFunctionHttpServerUnderTest(@NonNull Map<String, Object> properties) {
        properties.put("micronaut.server.context-path", "/");
        this.function = new Function(AzureFunction.defaultApplicationContextBuilder().properties(properties));
    }

    @Override
    public <I, O> HttpResponse<O> exchange(HttpRequest<I> request, Argument<O> bodyType) {
        try {
            HttpRequestMessage<Optional<String>> azureRequest = adaptRequest(request);
            ExecutionContext executionContext = new DefaultExecutionContext();
            HttpResponseMessage response = function.route(azureRequest, executionContext);
        return adaptResponse(response);
        } catch (UnsupportedEncodingException e) {
            return new SimpleHttpResponseFactory().status(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public <I, O, E> HttpResponse<O> exchange(HttpRequest<I> request, Argument<O> bodyType, Argument<E> errorType) {
        return exchange(request, bodyType);
    }

    private <I> HttpRequestMessage<Optional<String>> adaptRequest(HttpRequest<I> request) throws UnsupportedEncodingException {
        HttpMethod httpMethod = HttpMethod.valueOf(request.getMethodName());
        HttpRequestMessageBuilder<Optional<String>> builder =  HttpRequestMessageBuilder.builder(httpMethod, request.getUri().toString(), function.getApplicationContext());
        request.getHeaders().forEach((name, values) -> {
            for (String value : values) {
                builder.header(name, value);
            }
        });
        builder.body(body(request));
        return builder.build();
    }

    private <I> Optional<String> body(HttpRequest<I> request) throws UnsupportedEncodingException {
        if (request.getContentType().isPresent() && request.getContentType().get().equals(MediaType.APPLICATION_FORM_URLENCODED_TYPE)) {
            Optional<Map> body = request.getBody(Map.class);
            if (body.isPresent()) {
                return Optional.of(getDataString(body.get()));
            }
        }
        return BodyUtils.bodyAsString(
            getApplicationContext().getBean(JsonMapper.class),
            () -> request.getContentType().orElse(null),
            request::getCharacterEncoding,
            () -> request.getBody().orElse(null)
        );
    }

    private <O> HttpResponse<O> adaptResponse(HttpResponseMessage azureResponse) {
        MutableHttpResponse<O> response = new SimpleHttpResponseFactory().status(HttpStatus.valueOf(azureResponse.getStatusCode()));
        populateHeaders(response, azureResponse, STANDARD_HEADERS);
        populateHeaders(response, azureResponse, HEADERS_USED_IN_TEST_SUITE);
        Object azureBody = azureResponse.getBody();
        if (azureBody instanceof byte[]) {
            response.body(new String((byte[]) azureBody));
        }
        if (response.getStatus().getCode() >= 400) {
            throw new HttpClientResponseException("error: " + response.getStatus() + ":" + response.body(), response);
        }
        return response;
    }

    private static <O> void populateHeaders(@NonNull MutableHttpResponse<O> response,
                                            @NonNull HttpResponseMessage azureResponse,
                                            @NonNull Collection<String> headerNames) {
        for (String headerName : headerNames) {
            String headerValue = azureResponse.getHeader(headerName);
            if (StringUtils.isNotEmpty(headerValue)) {
                response.header(headerName, headerValue);
            }
        }
    }

    @Override
    public ApplicationContext getApplicationContext() {
        return function.getApplicationContext();
    }

    @Override
    public void close() throws IOException {
        if (function != null) {
            function.close();
        }
    }

    @Override
    @NonNull
    public Optional<Integer> getPort() {
        return Optional.of(8080);
    }

    private String getDataString(Map params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Object k : params.keySet()) {
            if (first) {
                first = false;
            } else {
                result.append("&");
            }
            result.append(URLEncoder.encode(k.toString(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(params.get(k).toString(), "UTF-8"));
        }
        return result.toString();
    }


    private final static List<String> HEADERS_USED_IN_TEST_SUITE = Arrays.asList("X-Test-Filter",
        "X-Captured-Remote-Address");

    private final static List<String> STANDARD_HEADERS = Arrays.asList(
        HttpHeaders.ACCEPT,
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
