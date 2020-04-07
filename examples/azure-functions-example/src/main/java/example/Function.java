/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package example;

import java.util.*;
import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import io.micronaut.azure.function.http.AzureHttpFunction;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Function extends AzureHttpFunction {
    /**
     * This function listens at endpoint "/api/*". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/*&code={your function key}
     * 2. curl "{your host}/api/*?name=HTTP%20Query&code={your function key}"
     * Function Key is not needed when running locally, it is used to invoke function deployed to Azure.
     * More details: https://aka.ms/functions_authorization_keys
     */
    @FunctionName("ExampleTrigger")
    @Override
    public HttpResponseMessage invoke(
            @HttpTrigger(
                name = "req", 
                methods = {HttpMethod.GET, HttpMethod.POST},
                route = "{*route}",
                authLevel = AuthorizationLevel.FUNCTION) 
                HttpRequestMessage<Optional<byte[]>> request,
                final ExecutionContext context) {
        return super.invoke(request, context);
    }
}
