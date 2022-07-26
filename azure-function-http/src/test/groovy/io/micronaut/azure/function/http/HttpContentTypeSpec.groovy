package io.micronaut.azure.function.http

import com.microsoft.azure.functions.HttpMethod
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.PendingFeature
import spock.lang.Specification

@MicronautTest
class HttpContentTypeSpec extends Specification {

    @PendingFeature(reason = "failing with BAD_REQUEST")
    void "POST form url encoded body binding to pojo works"() {
        given:
        FormFunction function = new FormFunction()

        when:
        HttpRequestMessageBuilder.AzureHttpResponseMessage response =
                function.request(HttpMethod.POST, "/form")
                        .body("message=World")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                        .invoke()

        then:
        response.statusCode == HttpStatus.OK.code
        response.bodyAsString == '{"message":"Hello World"}'

        cleanup:
        function.close()
    }

    @PendingFeature(reason = "failing with BAD_REQUEST")
    void "POST form url encoded body binding to pojo works if you don't specify body annotation"() {
        given:
        FormFunction function = new FormFunction()

        when:
        HttpRequestMessageBuilder.AzureHttpResponseMessage response =
                function.request(HttpMethod.POST, '/form/without-body-annotation')
                        .body("message=World")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                        .invoke()

        then:
        response.statusCode == HttpStatus.OK.code
        response.bodyAsString == '{"message":"Hello World"}'

        cleanup:
        function.close()
    }

    @PendingFeature(reason = "failing with BAD_REQUEST")
    void "POST form-url-encoded with Body annotation and a nested attribute"() {
        given:
        FormFunction function = new FormFunction()

        when:
        HttpRequestMessageBuilder.AzureHttpResponseMessage response =
                function.request(HttpMethod.POST, '/form/nested-attribute')
                        .body("message=World")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                        .invoke()

        then:
        response.statusCode == HttpStatus.OK.code
        response.bodyAsString == '{"message":"Hello World"}'

        cleanup:
        function.close()
    }

    void "POST application-json with Body annotation and a nested attribute"() {
        given:
        FormFunction function = new FormFunction()

        when:
        HttpRequestMessageBuilder.AzureHttpResponseMessage response =
                function.request(HttpMethod.POST, '/form/json-nested-attribute')
                        .body("{\"message\":\"World\"}")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .invoke()

        then:
        response.statusCode == HttpStatus.OK.code
        response.bodyAsString == '{"message":"Hello World"}'

        cleanup:
        function.close()
    }

    @PendingFeature(reason = "failing with BAD_REQUEST")
    void "POST application-json without Body annotation"() {
        given:
        FormFunction function = new FormFunction()

        when:
        HttpRequestMessageBuilder.AzureHttpResponseMessage response =
                function.request(HttpMethod.POST, '/form/json-without-body-annotation')
                        .body("{\"message\":\"World\"}")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .invoke()

        then:
        response.statusCode == HttpStatus.OK.code
        response.bodyAsString == '{"message":"Hello World"}'

        cleanup:
        function.close()
    }

    void "POST application-json with Body annotation and a nested attribute and Map return rendered as JSON"() {
        given:
        FormFunction function = new FormFunction()

        when:
        HttpRequestMessageBuilder.AzureHttpResponseMessage response =
                function.request(HttpMethod.POST, '/form/json-nested-attribute-with-map-return')
                        .body("{\"message\":\"World\"}")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .invoke()

        then:
        response.statusCode == HttpStatus.OK.code
        response.bodyAsString == '{"message":"Hello World"}'

        cleanup:
        function.close()
    }

    void "POST application-json with Body annotation and Object return rendered as JSON"() {
        given:
        FormFunction function = new FormFunction()

        when:
        HttpRequestMessageBuilder.AzureHttpResponseMessage response =
                function.request(HttpMethod.POST, '/form/json-with-body-annotation-and-with-object-return')
                        .body("{\"message\":\"World\"}")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .invoke()

        then:
        response.statusCode == HttpStatus.OK.code
        response.bodyAsString == '{"greeting":"Hello World"}'

        cleanup:
        function.close()
    }

    void "json with @Body annotation"() {
        given:
        FormFunction function = new FormFunction()

        when:
        HttpRequestMessageBuilder.AzureHttpResponseMessage response =
                function.request(HttpMethod.POST, "/form/json-with-body")
                        .body('{"message":"World"}')
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                        .invoke()

        then:
        response.statusCode == HttpStatus.OK.code
        response.bodyAsString == '{"message":"Hello World"}'

        cleanup:
        function.close()
    }
}
