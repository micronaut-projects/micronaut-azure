package io.micronaut.azure.function.http;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpResponseMessage;

import java.util.logging.LogManager;
import java.util.logging.Logger;

public class TestUtils {

    public static HttpResponseMessage invoke(AzureHttpFunction function, HttpRequestMessageBuilder<?> builder) {
        return function.route(
                builder.buildEncoded(),
                new DefaultExecutionContext()
            );
    }

    static class DefaultExecutionContext implements ExecutionContext {

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
    }
}
