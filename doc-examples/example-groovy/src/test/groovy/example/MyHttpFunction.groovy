package example

import com.microsoft.azure.functions.*
import com.microsoft.azure.functions.annotation.*
import io.micronaut.azure.function.http.AzureHttpFunction

class MyHttpFunction extends AzureHttpFunction { // <1>
    @FunctionName("ExampleTrigger") // <2>
    HttpResponseMessage invoke(
            @HttpTrigger(
                    name = "req",
                    methods = [HttpMethod.GET, HttpMethod.POST], // <3>
                    route = "{*route}", // <4>
                    authLevel = AuthorizationLevel.ANONYMOUS) // <5>
                    HttpRequestMessage<Optional<byte[]>> request, // <6>
            final ExecutionContext context) {
        return super.route(request, context) // <7>
    }
}
