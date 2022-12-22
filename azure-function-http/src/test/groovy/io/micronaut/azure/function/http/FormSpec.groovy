package io.micronaut.azure.function.http

import com.microsoft.azure.functions.HttpMethod
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import spock.lang.Issue
import spock.lang.Specification

class FormSpec extends Specification {

    @Issue("https://github.com/micronaut-projects/micronaut-azure/issues/120")
    void "x-www-form-urlencoded POST request with body parameters"() {
        given:
        AzureHttpFunction function = new AzureHttpFunction()

        def responseMessage = TestUtils.invoke(function, function
                .request(HttpMethod.POST, "/form/form-url-encoded")
                .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED)
                .body("message=bodyMessage"))

        expect:
        responseMessage.status.value() == HttpStatus.OK.code
        responseMessage.bodyAsString == 'Output: bodyMessage'
    }

    @Issue("https://github.com/micronaut-projects/micronaut-azure/issues/120")
    void "x-www-form-urlencoded POST request with body parameters using nested attribute"() {
        given:
        AzureHttpFunction function = new AzureHttpFunction()
        def responseMessage = TestUtils.invoke(function, function
                .request(HttpMethod.POST, "/form/form-url-encoded-nested-attribute")
                .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED)
                .body("message=bodyMessage"))

        expect:
        responseMessage.status.value() == HttpStatus.OK.code
        responseMessage.bodyAsString == 'Output: bodyMessage'
    }

    void "x-www-form-urlencoded POST request with request query parameters"() {
        given:
        AzureHttpFunction function = new AzureHttpFunction()
        def responseMessage = TestUtils.invoke(function, function
                .request(HttpMethod.POST, "/form/form-url-encoded")
                .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED)
                .parameter("message", "queryMessage"))

        expect:
        responseMessage.status.value() == HttpStatus.OK.code
        responseMessage.bodyAsString == 'Output: queryMessage'
    }

    void "x-www-form-urlencoded POST request with request query parameters using nested attribute"() {
        given:
        AzureHttpFunction function = new AzureHttpFunction()
        def responseMessage = TestUtils.invoke(function, function
                .request(HttpMethod.POST, "/form/form-url-encoded-nested-attribute")
                .header("Content-Type", MediaType.APPLICATION_FORM_URLENCODED)
                .parameter("message", "queryMessage"))

        expect:
        responseMessage.status.value() == HttpStatus.OK.code
        responseMessage.bodyAsString == 'Output: queryMessage'
    }
}
