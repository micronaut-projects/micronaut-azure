package io.micronaut.azure.function.http

import com.microsoft.azure.functions.HttpMethod
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import spock.lang.Issue
import spock.lang.PendingFeature
import spock.lang.Specification

class ParameterBindingSpec extends Specification {

    void "test URI parameters"() {

        given:
        AzureHttpFunction function = new AzureHttpFunction()
        def responseMessage = function
                .request(HttpMethod.GET, "/parameters/uri/Foo")
                .invoke()

        expect:
        responseMessage.statusCode == HttpStatus.OK.code
        responseMessage.getHeader(HttpHeaders.CONTENT_TYPE) == MediaType.TEXT_PLAIN
        responseMessage.bodyAsString == 'Hello Foo'

        cleanup:
        function.close()
    }

    void "test invalid HTTP method"() {
        given:
        AzureHttpFunction function = new AzureHttpFunction()
        def responseMessage = function
                .request(HttpMethod.POST, "/parameters/uri/Foo")
                .invoke()

        expect:
        responseMessage.statusCode == HttpStatus.METHOD_NOT_ALLOWED.code
        def allow = responseMessage.getHeader(HttpHeaders.ALLOW)
        allow == "HEAD,GET"

        cleanup:
        function.close()
    }

    void "test query value"() {

        given:
        AzureHttpFunction function = new AzureHttpFunction()
        def responseMessage = function
                .request(HttpMethod.GET, "/parameters/query")
                .parameter("q", "Foo")
                .invoke()


        expect:
        responseMessage.statusCode == HttpStatus.OK.code
        responseMessage.getHeader(HttpHeaders.CONTENT_TYPE) == MediaType.TEXT_PLAIN
        responseMessage.bodyAsString == 'Hello Foo'

        cleanup:
        function.close()
    }

    void "test all parameters"() {

        given:
        AzureHttpFunction function = new AzureHttpFunction()
        def responseMessage = function
                .request(HttpMethod.GET, "/parameters/allParams")
                .parameter("name", "Foo")
                .parameter("age", "20")
                .invoke()

        expect:
        responseMessage.statusCode == HttpStatus.OK.code
        responseMessage.getHeader(HttpHeaders.CONTENT_TYPE) == MediaType.TEXT_PLAIN
        responseMessage.bodyAsString == 'Hello Foo 20'

        cleanup:
        function.close()
    }

    void "test header value"() {
        given:
        AzureHttpFunction function = new AzureHttpFunction()
        def responseMessage = function
                .request(HttpMethod.GET, "/parameters/header")
                .header(HttpHeaders.CONTENT_TYPE, "text/plain;q=1.0")
                .invoke()

        expect:
        responseMessage.statusCode == HttpStatus.OK.code
        responseMessage.getHeader(HttpHeaders.CONTENT_TYPE) == MediaType.TEXT_PLAIN
        responseMessage.bodyAsString == 'Hello text/plain;q=1.0'

        cleanup:
        function.close()
    }

    void "test request and response"() {

        given:
        AzureHttpFunction function = new AzureHttpFunction()
        def responseMessage = function
                .request(HttpMethod.GET, "/parameters/reqAndRes")
                .invoke()


        expect:
        responseMessage.bodyAsString == 'Good'
        responseMessage.statusCode == HttpStatus.ACCEPTED.code
        responseMessage.getHeader(HttpHeaders.CONTENT_TYPE) == MediaType.TEXT_PLAIN

        cleanup:
        function.close()
    }

    void "test string body"() {

        given:
        AzureHttpFunction function = new AzureHttpFunction()
        def responseMessage = function
                .request(HttpMethod.POST, "/parameters/stringBody")
                .body("Foo")
                .header(HttpHeaders.CONTENT_TYPE, "text/plain")
                .invoke()

        expect:
        responseMessage.statusCode == HttpStatus.OK.code
        responseMessage.getHeader(HttpHeaders.CONTENT_TYPE) == MediaType.TEXT_PLAIN
        responseMessage.bodyAsString == 'Hello Foo'

        cleanup:
        function.close()
    }

    void "test writable"() {

        given:
        AzureHttpFunction function = new AzureHttpFunction()
        def responseMessage = function
                .request(HttpMethod.POST, "/parameters/writable")
                .body("Foo")
                .header(HttpHeaders.CONTENT_TYPE, "text/plain")
                .invoke()
        expect:
        responseMessage.statusCode == HttpStatus.CREATED.code
        responseMessage.getHeader(HttpHeaders.CONTENT_TYPE) == MediaType.TEXT_PLAIN
        responseMessage.bodyAsString == 'Hello Foo'
        responseMessage.getHeader("Foo") == 'Bar'

        cleanup:
        function.close()
    }

    void "test JSON POJO body"() {
        given:
        def json = '{"name":"bar","age":30}'
        AzureHttpFunction function = new AzureHttpFunction()
        def responseMessage = function
                .request(HttpMethod.POST, "/parameters/jsonBody")
                .body(json)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .invoke()

        expect:
        responseMessage.statusCode == HttpStatus.OK.code
        responseMessage.getHeader(HttpHeaders.CONTENT_TYPE) == MediaType.APPLICATION_JSON
        responseMessage.bodyAsString == json

        cleanup:
        function.close()
    }

    void "test JSON POJO body - invalid JSON"() {
        given:
        AzureHttpFunction function = new AzureHttpFunction()
        def json = '{"name":"bar","age":30'
        def responseMessage = function
                .request(HttpMethod.POST, "/parameters/jsonBody")
                .body(json)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .invoke()

        expect:
        responseMessage.statusCode == HttpStatus.BAD_REQUEST.code
        responseMessage.bodyAsString.contains("Error decoding JSON stream for type")

        cleanup:
        function.close()
    }

    void "test JSON POJO body with no @Body binds to arguments"() {
        given:
        AzureHttpFunction function = new AzureHttpFunction()
        def json = '{"name":"bar","age":20}'
        def responseMessage = function
                .request(HttpMethod.POST, "/parameters/jsonBodySpread")
                .body(json)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .invoke()


        expect:
        responseMessage.statusCode == HttpStatus.OK.code
        responseMessage.getHeader(HttpHeaders.CONTENT_TYPE) == MediaType.APPLICATION_JSON
        responseMessage.bodyAsString == json

        cleanup:
        function.close()
    }

    void "full Micronaut request and response"() {
        given:
        def json = '{"name":"bar","age":20}'
        AzureHttpFunction function = new AzureHttpFunction()
        def responseMessage = function
                .request(HttpMethod.POST, "/parameters/fullRequest")
                .body(json)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .invoke()


        expect:
        responseMessage.statusCode == HttpStatus.OK.code
        responseMessage.getHeader(HttpHeaders.CONTENT_TYPE) == MediaType.APPLICATION_JSON
        responseMessage.bodyAsString == json
        responseMessage.getHeader("Foo") == 'Bar'

        cleanup:
        function.close()
    }

    @PendingFeature
    @Issue("https://github.com/micronaut-projects/micronaut-aws/issues/1410")
    // this copies a test we have in micronaut-aws, MicronautLambdaHandlerSpec
    // this is failing with HTTP 415 UNSUPPORTED_MEDIA_TYPE
    // or is my test just wrong?
    void "full Micronaut form-url-encoded request and response"() {
        given:
        def body = 'message=World'
        AzureHttpFunction function = new AzureHttpFunction()
        def responseMessage = function
                .request(HttpMethod.POST, "/parameters/fullRequest")
                .body(body)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .invoke()


        expect:
        responseMessage.statusCode == HttpStatus.OK.code
        responseMessage.getHeader(HttpHeaders.CONTENT_TYPE) == MediaType.APPLICATION_JSON
        responseMessage.bodyAsString == body
        responseMessage.getHeader("Foo") == 'Bar'

        cleanup:
        function.close()
    }

    void "full Micronaut request and response - invalid JSON"() {
        given:
        AzureHttpFunction function = new AzureHttpFunction()
        def json = '{"name":"bar","age":20'
        def responseMessage = function
                .request(HttpMethod.POST, "/parameters/fullRequest")
                .body(json)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .invoke()

        expect:
        responseMessage.statusCode == HttpStatus.BAD_REQUEST.code
        responseMessage.bodyAsString.contains("Error decoding JSON stream for type")

        cleanup:
        function.close()
    }

//  TODO: multipart not directly supported by Azure
//
//    void "test multipart binding"() {
//        given:
//        def googleRequest = new MockGoogleRequest(HttpMethod.POST, "/parameters/multipart")
//        googleRequest.addParameter("foo", "bar")
//        googleRequest.parts.put("one", new MockGoogleHttpPart("one.json", '{"name":"bar","age":20}', "application/json"))
//        googleRequest.parts.put("two", new MockGoogleHttpPart("two.txt", 'Whatever', "text/plain"))
//        googleRequest.parts.put("three", new MockGoogleHttpPart("some.doc", 'My Doc', "application/octet-stream"))
//        googleRequest.parts.put("four", new MockGoogleHttpPart("raw.doc", 'Another Doc', "application/octet-stream"))
//        googleRequest.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA)
//        def googleResponse = new MockGoogleResponse()
//        new HttpFunction()
//                .service(googleRequest, googleResponse)
//
//        expect:
//        googleResponse.statusCode == HttpStatus.OK.code
//        googleResponse.text == 'Good: true'
//
//    }
}
