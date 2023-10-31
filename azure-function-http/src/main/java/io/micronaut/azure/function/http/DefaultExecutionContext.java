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

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.TraceContext;
import io.micronaut.core.annotation.Internal;

import java.util.Collections;
import java.util.Map;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Default execution context impl. used for testing.
 * @author Sergio del Amo
 * @since 5.0.0
 */
@Internal
public class DefaultExecutionContext implements ExecutionContext {

    @Override
    public Logger getLogger() {
        return LogManager.getLogManager().getLogger(DefaultHttpRequestMessageBuilder.class.getName());
    }

    @Override
    public String getInvocationId() {
        return getFunctionName();
    }

    @Override
    public String getFunctionName() {
        return "io.micronaut.azure.function.http.AzureHttpFunction";
    }

    @Override
    public TraceContext getTraceContext() {
        return new TraceContext() {
            @Override
            public String getTraceparent() {
                return null;
            }

            @Override
            public String getTracestate() {
                return null;
            }

            @Override
            public Map<String, String> getAttributes() {
                return Collections.emptyMap();
            }
        };
    }
}
