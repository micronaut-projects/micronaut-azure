package io.micronaut.azure.function.http

import com.microsoft.azure.functions.HttpMethod
import com.microsoft.azure.functions.HttpResponseMessage
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import spock.lang.Issue
import spock.lang.Specification

class FormSpec extends Specification {

    @Issue("https://github.com/micronaut-projects/micronaut-azure/issues/120")
    void "x-www-form-urlencoded POST request with body parameters"() {
        given:
        AzureHttpFunction function = new AzureHttpFunction()

        HttpResponseMessage responseMessage = function
                .request(HttpMethod.POST, "/form/form-url-encoded")
                .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED)
                .body("message=bodyMessage")
                .invoke()

        expect:
        responseMessage.statusCode == HttpStatus.OK.code
        responseMessage.body == 'Output: bodyMessage'

        cleanup:
        function.close()
    }

    @Issue("https://github.com/micronaut-projects/micronaut-azure/issues/120")
    void "x-www-form-urlencoded POST request with body parameters using nested attribute"() {
        given:
        AzureHttpFunction function = new AzureHttpFunction()
        HttpResponseMessage responseMessage = function
                .request(HttpMethod.POST, "/form/form-url-encoded-nested-attribute")
                .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED)
                .body("message=bodyMessage")
                .invoke()

        expect:
        responseMessage.statusCode == HttpStatus.OK.code
        responseMessage.body == 'Output: bodyMessage'

        cleanup:
        function.close()
    }

    void "x-www-form-urlencoded POST request with request query parameters"() {
        given:
        AzureHttpFunction function = new AzureHttpFunction()
        HttpResponseMessage responseMessage = function
                .request(HttpMethod.POST, "/form/form-url-encoded")
                .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED)
                .parameter("message", "queryMessage")
                .invoke()

        expect:
        responseMessage.statusCode == HttpStatus.OK.code
        responseMessage.body == 'Output: queryMessage'

        cleanup:
        function.close()
    }

    void "x-www-form-urlencoded POST request with request query parameters using nested attribute"() {
        given:
        AzureHttpFunction function = new AzureHttpFunction()
        HttpResponseMessage responseMessage = function
                .request(HttpMethod.POST, "/form/form-url-encoded-nested-attribute")
                .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED)
                .parameter("message", "queryMessage")
                .invoke()

        expect:
        responseMessage.statusCode == HttpStatus.OK.code
        responseMessage.body == 'Output: queryMessage'

        cleanup:
        function.close()
    }
}
