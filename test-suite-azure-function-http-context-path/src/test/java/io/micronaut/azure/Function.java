package io.micronaut.azure;

import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import io.micronaut.azure.function.http.AzureHttpFunction;

public class Function extends AzureHttpFunction {

    @FunctionName("ExampleTrigger")
    public HttpResponseMessage invoke(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET, HttpMethod.POST},
                route = "{*route}",
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
                final ExecutionContext context
    ) {
        return super.route(request, context);
    }
}
