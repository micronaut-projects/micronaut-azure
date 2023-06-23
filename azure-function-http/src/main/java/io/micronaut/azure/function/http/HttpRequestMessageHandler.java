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

import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import io.micronaut.context.ApplicationContext;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.servlet.http.BodyBuilder;
import io.micronaut.servlet.http.ServletExchange;
import io.micronaut.servlet.http.ServletHttpHandler;
import jakarta.inject.Singleton;

import java.util.Optional;

/**
 *
 * @author Tim Yates
 * @since 5.0.0
 */
@Internal
@Singleton
class HttpRequestMessageHandler extends ServletHttpHandler<HttpRequestMessage<Optional<String>>, HttpResponseMessage> {

    HttpRequestMessageHandler(ApplicationContext applicationContext) {
        super(applicationContext, applicationContext.getBean(ConversionService.class));
    }

    @Override
    protected ServletExchange<HttpRequestMessage<Optional<String>>, HttpResponseMessage> createExchange(
        HttpRequestMessage<Optional<String>> request,
        HttpResponseMessage response
    ) {
        return new AzureFunctionHttpRequest<>(
            request,
            new AzureFunctionHttpResponse<>(
                getApplicationContext().getConversionService(),
                getApplicationContext().getBean(BinaryContentConfiguration.class)
            ),
            new DefaultExecutionContext(),
            applicationContext.getConversionService(),
            applicationContext.getBean(BinaryContentConfiguration.class),
            applicationContext.getBean(BodyBuilder.class)
        );
    }
}
