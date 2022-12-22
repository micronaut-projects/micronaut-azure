package io.micronaut.azure.function.http;

import com.microsoft.azure.functions.HttpResponseMessage;

public class TestUtils {

    public static AzureHttpResponseMessage invoke(AzureHttpFunction function, HttpRequestMessageBuilder<?> builder) {
        HttpResponseMessage result = function.route(
                builder.buildEncoded(),
                new DefaultExecutionContext()
            );
        return new AzureHttpResponseMessageAdapter(result);
    }
}
