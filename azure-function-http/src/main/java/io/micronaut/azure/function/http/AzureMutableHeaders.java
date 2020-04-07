package io.micronaut.azure.function.http;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.http.MutableHttpHeaders;

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
    AzureMutableHeaders(Map<String, String> map, ConversionService<?> conversionService) {
        super(map, conversionService);
    }

    @Override
    public MutableHttpHeaders add(CharSequence header, CharSequence value) {
        if (header != null && value != null) {
            map.put(header.toString(), value.toString());
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
}
