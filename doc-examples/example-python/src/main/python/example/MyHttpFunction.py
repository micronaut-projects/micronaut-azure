from typing import Annotated

from com.microsoft.azure.functions import ExecutionContext, HttpMethod, HttpRequestMessage, HttpResponseMessage
from com.microsoft.azure.functions.annotation import AuthorizationLevel, FunctionName, HttpTrigger
from java.util import Optional
from micronaut.azure.function import AzureFunction
from micronaut.azure.function.http import AzureHttpFunction


class MyHttpFunction:  # <1>
    def __init__(self):
        self.function = AzureHttpFunction(AzureFunction.defaultApplicationContextBuilder())

    @FunctionName("ExampleTrigger")  # <2>
    def invoke(self,
               request: Annotated[HttpRequestMessage[Optional[str]], HttpTrigger(
                   name="req",
                   methods=[HttpMethod.GET, HttpMethod.POST],  # <3>
                   route="{*route}",  # <4>
                   authLevel=AuthorizationLevel.ANONYMOUS,  # <5>
               )],  # <6>
               context: ExecutionContext) -> HttpResponseMessage:
        return self.function.route(request, context)  # <7>

    def close(self) -> None:
        self.function.close()
