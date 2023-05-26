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

import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.ConvertibleValues;
import io.micronaut.core.convert.value.ConvertibleValuesMap;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.bind.binders.DefaultBodyAnnotationBinder;
import io.micronaut.http.codec.MediaTypeCodecRegistry;
import io.micronaut.servlet.http.ServletBodyBinder;
import io.netty.handler.codec.http.QueryStringDecoder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Extends the body binding abilities of {@link ServletBodyBinder} for Azure.
 *
 * @param <T> The body type
 * @author sbodvanski
 * @since 3.4.1
 */
public class AzureServletBodyBinder<T> extends ServletBodyBinder<T> {

    /**
     * Default constructor.
     *
     * @param conversionService      The conversion service
     * @param mediaTypeCodecRegistry The codec registry
     * @param defaultBodyAnnotationBinder The delegate default body binder
     */
    protected AzureServletBodyBinder(ConversionService conversionService,
                                     MediaTypeCodecRegistry mediaTypeCodecRegistry,
                                     DefaultBodyAnnotationBinder<T> defaultBodyAnnotationBinder) {
        super(conversionService, mediaTypeCodecRegistry, defaultBodyAnnotationBinder);
    }

    @Override
    public BindingResult<T> bind(ArgumentConversionContext<T> context, HttpRequest<?> source) {
        final MediaType mediaType = source.getContentType().orElse(MediaType.APPLICATION_JSON_TYPE);
        if (source instanceof AzureFunctionHttpRequest && isFormSubmitted(mediaType)) {
            AzureFunctionHttpRequest<?> azureFunctionRequest = (AzureFunctionHttpRequest<?>) source;
            Optional<String> formBody = azureFunctionRequest.getNativeRequest().getBody();
            if (formBody.isPresent()) {
                Optional<ConvertibleValues<?>> bodyParameters = formUrlEncodedBodyToConvertibleValues(formBody.get());
                if (bodyParameters.isPresent()) {
                    ConvertibleValues<?> convertibleBodyParameters = bodyParameters.get();
                    Argument<T> argument = context.getArgument();
                    Optional<String> nestedBodyName = argument.getAnnotationMetadata().stringValue(Body.class);
                    if (nestedBodyName.isPresent()) {
                        return () -> convertibleBodyParameters.get(nestedBodyName.get(), context);
                    }
                    Optional<T> result = conversionService.convert(convertibleBodyParameters.asMap(), context);
                    return () -> result;
                }
            }

        }
        return super.bind(context, source);
    }

    private Optional<ConvertibleValues<?>> formUrlEncodedBodyToConvertibleValues(String body) {
        ConvertibleValuesMap<List<String>> convertibleFormParameters = formUrlEncodedBodyToMap(body)
            .map(ConvertibleValuesMap::new).orElse(null);
        return Optional.ofNullable(convertibleFormParameters);
    }

    private Optional<Map<String, List<String>>> formUrlEncodedBodyToMap(String body) {
        QueryStringDecoder decoder = new QueryStringDecoder(body, false);
        Map<String, List<String>> parameters = decoder.parameters();
        return CollectionUtils.isEmpty(parameters) ? Optional.empty() :
            Optional.of(parameters);
    }

    private boolean isFormSubmitted(MediaType contentType) {
        return MediaType.APPLICATION_FORM_URLENCODED_TYPE.equals(contentType) || MediaType.MULTIPART_FORM_DATA_TYPE.equals(contentType);
    }
}
