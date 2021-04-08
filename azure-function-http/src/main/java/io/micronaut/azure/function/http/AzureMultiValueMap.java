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
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.ConvertibleMultiValues;
import io.micronaut.core.convert.value.MutableConvertibleMultiValuesMap;
import io.micronaut.core.util.ArgumentUtils;

import io.micronaut.core.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class for representing azure headers and parameters.
 *
 * @author graemerocher
 * @since 1.0
 */
@Internal
class AzureMultiValueMap implements ConvertibleMultiValues<String> {
    final MutableConvertibleMultiValuesMap<String> map;
    final ConversionService<?> conversionService;

    /**
     * Default constructor.
     * @param map The map
     * @param conversionService The conversion service
     */
    AzureMultiValueMap(Map<CharSequence, List<String>> map, ConversionService<?> conversionService) {
        this.map = new MutableConvertibleMultiValuesMap<>(map, conversionService);
        this.conversionService = conversionService;
    }

    @Override
    public List<String> getAll(CharSequence name) {
        if (name != null) {
            String v = map.get(name.toString());
            if (v != null) {
                return Collections.singletonList(v);
            }
        }
        return Collections.emptyList();
    }

    @Nullable
    @Override
    public String get(CharSequence name) {
        ArgumentUtils.requireNonNull("name", name);
        return map.get(name.toString());

    }

    @Override
    public Set<String> names() {
        return map.names();
    }

    @Override
    public Collection<List<String>> values() {
        return map.values();
    }

    @Override
    public <T> Optional<T> get(CharSequence name, ArgumentConversionContext<T> conversionContext) {
        return map.get(name, conversionContext);
    }
}
