from com.microsoft.azure.functions import HttpMethod
from micronaut.azure.function.http import DefaultExecutionContext
from micronaut.http import HttpStatus
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test

from example.MyHttpFunction import MyHttpFunction


@MicronautTest
class MyHttpFunctionTest:

    @Test
    def test_routes_requests_to_controllers(self) -> None:
        function = MyHttpFunction()
        try:
            request = function.function.request(HttpMethod.GET, "/hello").buildEncoded()
            response = function.invoke(request, DefaultExecutionContext())

            assert response.getStatusCode() == HttpStatus.OK.getCode()
            assert response.getBody() == "Hello World"
        finally:
            function.close()
