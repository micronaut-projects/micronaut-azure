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

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.http.MutableHttpHeaders;

import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link MutableHttpHeaders} for Azure.
 *
 * @author graemerocher
 * @since 1.0
 */
@Internal
class AzureMutableHeaders extends AzureMultiValueMap implements MutableHttpHeaders {
    /**
     * Default constructor.
     * @param map The target map. Never null
     * @param conversionService The conversion service
     */
    AzureMutableHeaders(Map<CharSequence, List<String>> map, ConversionService conversionService) {
        super(map, conversionService);
    }

    @Override
    public MutableHttpHeaders add(CharSequence header, CharSequence value) {
        if (header != null && value != null) {
            map.add(header, value.toString());
        }
        return this;
    }

    @Override
    public MutableHttpHeaders remove(CharSequence header) {
        if (header != null) {
            map.remove(header.toString());
        }
        return this;
    }

    @Override
    public void setConversionService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }
}
