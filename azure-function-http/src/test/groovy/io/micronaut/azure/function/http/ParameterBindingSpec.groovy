package io.micronaut.azure.function.http

import com.microsoft.azure.functions.ExecutionContext
import com.microsoft.azure.functions.HttpMethod
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import spock.lang.Specification

class ParameterBindingSpec extends Specification {

    void "test URI parameters"() {

        given:
        def responseMessage = new AzureHttpFunction()
                .request(HttpMethod.GET, "/parameters/uri/Foo")
                .invoke()

        expect:
        responseMessage.statusCode == HttpStatus.OK.code
        responseMessage.getHeader(HttpHeaders.CONTENT_TYPE) == MediaType.TEXT_PLAIN
        responseMessage.bodyAsString == 'Hello Foo'
    }

    void "test invalid HTTP method"() {
        given:
        def responseMessage = new AzureHttpFunction()
                .request(HttpMethod.POST, "/parameters/uri/Foo")
                .invoke()

        expect:
        responseMessage.statusCode == HttpStatus.METHOD_NOT_ALLOWED.code
        def allow = responseMessage.getHeader(HttpHeaders.ALLOW)
        allow == "HEAD,GET"
    }

    void "test query value"() {

        given:
        def responseMessage = new AzureHttpFunction()
                .request(HttpMethod.GET, "/parameters/query")
                .parameter("q", "Foo")
                .invoke()


        expect:
        responseMessage.statusCode == HttpStatus.OK.code
        responseMessage.getHeader(HttpHeaders.CONTENT_TYPE) == MediaType.TEXT_PLAIN
        responseMessage.bodyAsString == 'Hello Foo'
    }

    void "test all parameters"() {

        given:
        def responseMessage = new AzureHttpFunction()
                .request(HttpMethod.GET, "/parameters/allParams")
                .parameter("name", "Foo")
                .parameter("age", "20")
                .invoke()

        expect:
        responseMessage.statusCode == HttpStatus.OK.code
        responseMessage.getHeader(HttpHeaders.CONTENT_TYPE) == MediaType.TEXT_PLAIN
        responseMessage.bodyAsString == 'Hello Foo 20'
    }

    void "test header value"() {
        given:
        def responseMessage = new AzureHttpFunction()
                .request(HttpMethod.GET, "/parameters/header")
                .header(HttpHeaders.CONTENT_TYPE, "text/plain;q=1.0")
                .invoke()

        expect:
        responseMessage.statusCode == HttpStatus.OK.code
        responseMessage.getHeader(HttpHeaders.CONTENT_TYPE) == MediaType.TEXT_PLAIN
        responseMessage.bodyAsString == 'Hello text/plain;q=1.0'
    }

    void "test request and response"() {

        given:
        def responseMessage = new AzureHttpFunction()
                .request(HttpMethod.GET, "/parameters/reqAndRes")
                .invoke()


        expect:
        responseMessage.bodyAsString == 'Good'
        responseMessage.statusCode == HttpStatus.ACCEPTED.code
        responseMessage.getHeader(HttpHeaders.CONTENT_TYPE) == MediaType.TEXT_PLAIN
    }

    void "test string body"() {

        given:
        def responseMessage = new AzureHttpFunction()
                .request(HttpMethod.POST, "/parameters/stringBody")
                .body("Foo")
                .header(HttpHeaders.CONTENT_TYPE, "text/plain")
                .invoke()

        expect:
        responseMessage.statusCode == HttpStatus.OK.code
        responseMessage.getHeader(HttpHeaders.CONTENT_TYPE) == MediaType.TEXT_PLAIN
        responseMessage.bodyAsString == 'Hello Foo'
    }

    void "test writable"() {

        given:
        def responseMessage = new AzureHttpFunction()
                .request(HttpMethod.POST, "/parameters/writable")
                .body("Foo")
                .header(HttpHeaders.CONTENT_TYPE, "text/plain")
                .invoke()
        expect:
        responseMessage.statusCode == HttpStatus.CREATED.code
        responseMessage.getHeader(HttpHeaders.CONTENT_TYPE) == MediaType.TEXT_PLAIN
        responseMessage.bodyAsString == 'Hello Foo'
        responseMessage.getHeader("Foo") == 'Bar'
    }

    void "test JSON POJO body"() {


        given:
        def json = '{"name":"bar","age":30}'
        def responseMessage = new AzureHttpFunction()
                .request(HttpMethod.POST, "/parameters/jsonBody")
                .body(json)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .invoke()

        expect:
        responseMessage.statusCode == HttpStatus.OK.code
        responseMessage.getHeader(HttpHeaders.CONTENT_TYPE) == MediaType.APPLICATION_JSON
        responseMessage.bodyAsString == json
    }

    void "test JSON POJO body - invalid JSON"() {

        given:
        def json = '{"name":"bar","age":30'
        def responseMessage = new AzureHttpFunction()
                .request(HttpMethod.POST, "/parameters/jsonBody")
                .body(json)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .invoke()

        expect:
        responseMessage.statusCode == HttpStatus.BAD_REQUEST.code
        responseMessage.bodyAsString.contains("Error decoding JSON stream for type")
    }

    void "test JSON POJO body with no @Body binds to arguments"() {

        given:
        def json = '{"name":"bar","age":20}'
        def responseMessage = new AzureHttpFunction()
                .request(HttpMethod.POST, "/parameters/jsonBodySpread")
                .body(json)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .invoke()


        expect:
        responseMessage.statusCode == HttpStatus.OK.code
        responseMessage.getHeader(HttpHeaders.CONTENT_TYPE) == MediaType.APPLICATION_JSON
        responseMessage.bodyAsString == json
    }

    void "full Micronaut request and response"() {
        given:
        def json = '{"name":"bar","age":20}'
        def responseMessage = new AzureHttpFunction()
                .request(HttpMethod.POST, "/parameters/fullRequest")
                .body(json)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .invoke()


        expect:
        responseMessage.statusCode == HttpStatus.OK.code
        responseMessage.getHeader(HttpHeaders.CONTENT_TYPE) == MediaType.APPLICATION_JSON
        responseMessage.bodyAsString == json
        responseMessage.getHeader("Foo") == 'Bar'
    }


    void "full Micronaut request and response - invalid JSON"() {
        given:
        def json = '{"name":"bar","age":20'
        def responseMessage = new AzureHttpFunction()
                .request(HttpMethod.POST, "/parameters/fullRequest")
                .body(json)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .invoke()

        expect:
        responseMessage.statusCode == HttpStatus.BAD_REQUEST.code
        responseMessage.bodyAsString.contains("Error decoding JSON stream for type")
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