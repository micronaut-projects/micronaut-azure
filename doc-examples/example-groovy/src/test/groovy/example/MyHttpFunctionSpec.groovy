package example

import com.microsoft.azure.functions.HttpMethod
import io.micronaut.http.HttpStatus
import spock.lang.Specification

class MyHttpFunctionSpec extends Specification {

    void "routes requests to controllers"() {
        given:
        MyHttpFunction function = new MyHttpFunction()

        when:
        def response = function.request(HttpMethod.GET, "/hello").invoke()

        then:
        response.statusCode == HttpStatus.OK.code
        response.body == "Hello World"

        cleanup:
        function.close()
    }
}
