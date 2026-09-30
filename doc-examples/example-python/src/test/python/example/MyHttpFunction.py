from typing import Annotated

from com.microsoft.azure.functions import ExecutionContext, HttpMethod, HttpRequestMessage, HttpResponseMessage
from com.microsoft.azure.functions.annotation import AuthorizationLevel, FunctionName, HttpTrigger
from java.util import Optional
from micronaut.azure.function.http import AzureHttpFunction


class MyHttpFunction(AzureHttpFunction):
    """Test support: shows that the Azure HTTP adapter routes to the Python controllers of the application.

    This is not a guide snippet. The class deployed as the Azure Function entry point is the Java one of
    example-java, because the Azure Functions runtime instantiates it before the Python runtime exists.
    """

    @FunctionName("ExampleTrigger")
    def invoke(self,
               request: Annotated[HttpRequestMessage[Optional[str]], HttpTrigger(
                   name="req",
                   methods=[HttpMethod.GET, HttpMethod.POST],
                   route="{*route}",
                   authLevel=AuthorizationLevel.ANONYMOUS,
               )],
               context: ExecutionContext) -> HttpResponseMessage:
        return super().route(request, context)
