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
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.HttpStatusType;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.http.HttpHeaders;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Response builder implementation. Used for testing.
 */
@Internal
public class ResponseBuilder implements HttpResponseMessage.Builder, HttpResponseMessage, HttpHeaders {
    private HttpStatusType status = HttpStatus.OK;
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
