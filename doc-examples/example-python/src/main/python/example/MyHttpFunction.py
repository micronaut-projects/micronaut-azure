from typing import Annotated

from com.microsoft.azure.functions import ExecutionContext, HttpMethod, HttpRequestMessage, HttpResponseMessage
from com.microsoft.azure.functions.annotation import AuthorizationLevel, FunctionName, HttpTrigger
from java.util import Optional
from micronaut.azure.function.http import AzureHttpFunction


class MyHttpFunction(AzureHttpFunction):  # <1>
    @FunctionName("ExampleTrigger")  # <2>
    def invoke(self,
               request: Annotated[HttpRequestMessage[Optional[str]], HttpTrigger(
                   name="req",
                   methods=[HttpMethod.GET, HttpMethod.POST],  # <3>
                   route="{*route}",  # <4>
                   authLevel=AuthorizationLevel.ANONYMOUS,  # <5>
               )],  # <6>
               context: ExecutionContext) -> HttpResponseMessage:
        return super().route(request, context)  # <7>
