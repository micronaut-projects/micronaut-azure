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

import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.HttpStatusType;
import io.micronaut.azure.function.http.ResponseBuilder;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.cookie.Cookies;
import io.micronaut.http.netty.cookies.NettyCookie;
import io.micronaut.json.JsonMapper;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cookie.ClientCookieEncoder;

import java.net.URI;
import java.util.*;

/**
 * TODO: Write JavaDoc
 */
@Internal
public final class AzureRequestEventFactory {

    private static final String COMMA = ",";

    private AzureRequestEventFactory() {
    }

    @NonNull
    public static HttpRequestMessage<Optional<String>> create(@NonNull HttpRequest<?> request, JsonMapper jsonMapper) {
        Map<String, String> headers = new LinkedHashMap<>();
        Map<String, List<String>> multiHeaders = new LinkedHashMap<>();
        request.getHeaders().forEach((name, values) -> {
            if (values.size() > 1) {
                multiHeaders.put(name, values);
            } else {
                headers.put(name, values.get(0));
            }
        });
        try {
            Cookies cookies = request.getCookies();
            boolean many = cookies.getAll().size() > 1;
            cookies.forEach((s, cookie) -> {
                if (cookie instanceof NettyCookie nettyCookie) {
                    if (many) {
                        multiHeaders.computeIfAbsent(HttpHeaders.COOKIE, s1 -> new ArrayList<>())
                            .add(ClientCookieEncoder.STRICT.encode(nettyCookie.getNettyCookie()));
                    } else {
                        headers.put(HttpHeaders.COOKIE, ClientCookieEncoder.STRICT.encode(nettyCookie.getNettyCookie()));
                    }
                }
            });
        } catch (UnsupportedOperationException e) {
            //not all request types support retrieving cookies
        }
        return new HttpRequestMessage<>() {

            @Override
            public Map<String, String> getHeaders() {
                return headers;
            }

            @Override
            public Map<String, String> getQueryParameters() {
                Map<String, String> result = new HashMap<>();
                for (String name : request.getParameters().names()) {
                    result.put(name, String.join(COMMA, request.getParameters().getAll(name)));
                }
                return result;
            }

            @Override
            public URI getUri() {
                return request.getUri();
            }

            @Override
            public HttpMethod getHttpMethod() {
                return HttpMethod.valueOf(request.getMethod().name());
            }

            @Override
            public Optional<String> getBody() {
                return request.getBody()
                    .flatMap(b -> BodyUtils.bodyAsString(jsonMapper,
                        () -> request.getContentType().orElse(null),
                        request::getCharacterEncoding,
                        () -> b));
            }

            @Override
            public HttpResponseMessage.Builder createResponseBuilder(HttpStatus status) {
                return new ResponseBuilder().status(status);
            }

            @Override
            public HttpResponseMessage.Builder createResponseBuilder(HttpStatusType status) {
                return new ResponseBuilder().status(status);
            }
        };
    }
}
