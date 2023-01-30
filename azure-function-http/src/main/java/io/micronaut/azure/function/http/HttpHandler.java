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

import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import io.micronaut.context.ApplicationContext;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.servlet.http.ServletExchange;
import io.micronaut.servlet.http.ServletHttpHandler;
import io.netty.util.internal.MacAddressUtil;
import io.netty.util.internal.PlatformDependent;

import java.util.Optional;

/**
 * @author Sergio del Amo
 * @since 5.0.0
 */
@Internal
public class HttpHandler extends ServletHttpHandler<HttpRequestMessage<Optional<String>>, HttpResponseMessage> {

    public HttpHandler(ApplicationContext applicationContext) {
        super(init(applicationContext), applicationContext.getBean(ConversionService.class));
    }

    private static ApplicationContext init(ApplicationContext applicationContext) {
        setIoNettyMachineIdSystemProperty();
        return applicationContext;
    }

    private static void setIoNettyMachineIdSystemProperty() {
        byte[] bestMacAddr = new byte[8];
        PlatformDependent.threadLocalRandom().nextBytes(bestMacAddr);
        System.setProperty("io.netty.machineId", MacAddressUtil.formatAddress(bestMacAddr));
    }

    @Override
    public boolean isRunning() {
        return super.getApplicationContext().isRunning();
    }

    @Override
    protected ServletExchange<HttpRequestMessage<Optional<String>>, HttpResponseMessage> createExchange(HttpRequestMessage<Optional<String>> request, HttpResponseMessage response) {
        throw new UnsupportedOperationException("Creating the exchange directly is not supported");
    }
}
