/*
 * Copyright 2017-2022 original authors
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

/**
 * Adapter between {@link HttpResponseMessage} and {@link AzureHttpResponseMessage}.
 * @author Sergio del Amo
 * @since 5.0.0
 */
@Internal
public class AzureHttpResponseMessageAdapter implements AzureHttpResponseMessage {
    private final HttpResponseMessage result;

    public AzureHttpResponseMessageAdapter(HttpResponseMessage result) {
        this.result = result;

    }

    @Override
    public HttpStatusType getStatus() {
        return result.getStatus();
    }

    @Override
    public String getHeader(String key) {
        return result.getHeader(key);
    }

    @Override
    public Object getBody() {
        return result.getBody();
    }
}
