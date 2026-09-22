from com.microsoft.azure.functions import HttpMethod
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
            response = function.request(HttpMethod.GET, "/hello").invoke()

            assert response.getStatusCode() == HttpStatus.OK.getCode()
            assert response.getBody() == "Hello World"
        finally:
            function.close()
