package io.micronaut.azure

import com.microsoft.azure.functions.HttpResponseMessage
import com.microsoft.azure.functions.HttpStatus
import io.micronaut.http.HttpMethod
import io.micronaut.web.router.Router
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class DemoFunctionSpec extends Specification {

    @Shared
    @AutoCleanup
    Function function = new Function()

    void "test function"() {
        when:"The function is executed"
        HttpResponseMessage response = function.request(HttpMethod.GET, "/test/demo").invoke();

        then:"The response is correct"
        response.status == HttpStatus.OK
        response.body == "Example Response"
    }

    void "check routes"() {
        given:
        Router router = function.applicationContext.getBean(Router)

        expect:
        router.GET("/test/demo").present
    }
}
