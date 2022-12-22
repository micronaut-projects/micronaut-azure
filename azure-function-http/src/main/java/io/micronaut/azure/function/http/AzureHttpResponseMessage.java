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
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Nullable;
import java.nio.charset.StandardCharsets;

/**
 * Extends {@link HttpResponseMessage} to provide a utlity method to return the body as a String.
 * @author Sergio del Amo
 * @since 5.0.0
 */
@Internal
public interface AzureHttpResponseMessage extends HttpResponseMessage {
    /**
     * @return The body as a string.
     */
    @Nullable
    default String getBodyAsString() {
        Object body = getBody();
        if (body instanceof byte[]) {
            return new String((byte[]) body, StandardCharsets.UTF_8);
        } else if (body != null) {
            return body.toString();
        }
        return null;
    }
}
