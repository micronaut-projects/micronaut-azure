package example

import com.microsoft.azure.functions.ExecutionContext
import com.microsoft.azure.functions.HttpMethod
import com.microsoft.azure.functions.HttpRequestMessage
import com.microsoft.azure.functions.HttpResponseMessage

import com.microsoft.azure.functions.annotation.AuthorizationLevel
import com.microsoft.azure.functions.annotation.FunctionName
import com.microsoft.azure.functions.annotation.HttpTrigger
import io.micronaut.azure.function.http.AzureHttpFunction

class MyHttpFunction extends AzureHttpFunction { // <1>
    @FunctionName("ExampleTrigger") // <2>
    HttpResponseMessage invoke(
            @HttpTrigger(
                    name = "req",
                    methods = [HttpMethod.GET, HttpMethod.POST], // <3>
                    route = "{*route}", // <4>
                    authLevel = AuthorizationLevel.ANONYMOUS) // <5>
                    HttpRequestMessage<Optional<String>> request, // <6>
            final ExecutionContext context) {
        return super.route(request, context) // <7>
    }
}
