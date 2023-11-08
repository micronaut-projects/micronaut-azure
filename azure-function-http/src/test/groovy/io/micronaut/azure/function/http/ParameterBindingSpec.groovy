package io.micronaut.azure.function.http

import com.microsoft.azure.functions.HttpMethod
import com.microsoft.azure.functions.HttpRequestMessage
import com.microsoft.azure.functions.HttpResponseMessage
import com.microsoft.azure.functions.HttpStatusType
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import spock.lang.Specification

class ParameterBindingSpec extends Specification {

    void "test URI parameters"() {

        given:
        AzureHttpFunction function = new AzureHttpFunction()
        HttpResponseMessage responseMessage = function
                .request(HttpMethod.GET, "/parameters/uri/Foo")
                .invoke()

        expect:
        responseMessage.statusCode == HttpStatus.OK.code
        responseMessage.getHeader(HttpHeaders.CONTENT_TYPE) == MediaType.APPLICATION_JSON
        responseMessage.body == 'Hello Foo'

        cleanup:
        function.close()
    }

    void "test invalid HTTP method"() {
        given:
        AzureHttpFunction function = new AzureHttpFunction()
        HttpResponseMessage responseMessage = function
                .request(HttpMethod.POST, "/parameters/uri/Foo")
                .invoke()

        expect:
        responseMessage.statusCode == HttpStatus.METHOD_NOT_ALLOWED.code
        String allow = responseMessage.getHeader(HttpHeaders.ALLOW)
        allow == "HEAD,GET"

        cleanup:
        function.close()
    }

    void "test query value"() {

        given:
        AzureHttpFunction function = new AzureHttpFunction()
        HttpResponseMessage responseMessage = function
                .request(HttpMethod.GET, "/parameters/query")
                .parameter("q", "Foo")
                .invoke()

        expect:
        responseMessage.statusCode == HttpStatus.OK.code
        responseMessage.getHeader(HttpHeaders.CONTENT_TYPE) == MediaType.APPLICATION_JSON
        responseMessage.body == 'Hello Foo'

        cleanup:
        function.close()
    }

    void "test all parameters"() {

        given:
        AzureHttpFunction function = new AzureHttpFunction()

        HttpResponseMessage responseMessage = function
                .request(HttpMethod.GET, "/parameters/allParams")
                .parameter("name", "Foo")
                .parameter("age", "20")
                .invoke()

        expect:
        responseMessage.statusCode == HttpStatus.OK.code
        responseMessage.getHeader(HttpHeaders.CONTENT_TYPE) == MediaType.APPLICATION_JSON
        responseMessage.body == 'Hello Foo 20'

        cleanup:
        function.close()
    }

    void "test header value"() {
        given:
        AzureHttpFunction function = new AzureHttpFunction()
        HttpResponseMessage responseMessage = function
                .request(HttpMethod.GET, "/parameters/header")
                .header(HttpHeaders.CONTENT_TYPE, "text/plain;q=1.0")
                .invoke()

        expect:
        responseMessage.statusCode == HttpStatus.OK.code
        responseMessage.getHeader(HttpHeaders.CONTENT_TYPE) == MediaType.APPLICATION_JSON
        responseMessage.body == 'Hello text/plain;q=1.0'

        cleanup:
        function.close()
    }

    void "test request and response"() {
        given:
        AzureHttpFunction function = new AzureHttpFunction()
        HttpResponseMessage responseMessage = function
                .request(HttpMethod.GET, "/parameters/reqAndRes")
                .invoke()

        expect:
        responseMessage.body == 'Good'
        responseMessage.statusCode == HttpStatus.ACCEPTED.code
        responseMessage.getHeader(HttpHeaders.CONTENT_TYPE) == MediaType.TEXT_PLAIN

        cleanup:
        function.close()
    }

    void "ExecutionContext can be bound as a controller method parameter"() {
        given:
        AzureHttpFunction function = new AzureHttpFunction()

        HttpRequestMessage<Optional<String>> request = createHttpRequestMessage("/parameters/executionContext")
        HttpResponseMessage responseMessage = function
                .route(request, new DefaultExecutionContext())

        expect:
        responseMessage.statusCode == HttpStatus.OK.code

        cleanup:
        function.close()
    }

    void "TraceContext can be bound as a controller method parameter"() {
        given:
        AzureHttpFunction function = new AzureHttpFunction()

        HttpRequestMessage<Optional<String>> request = createHttpRequestMessage("/parameters/traceContext")
        HttpResponseMessage responseMessage = function
                .route(request, new DefaultExecutionContext())

        expect:
        responseMessage.statusCode == HttpStatus.OK.code

        cleanup:
        function.close()
    }

    void "java.util.logging.Logger can be bound as a controller method parameter"() {
        given:
        AzureHttpFunction function = new AzureHttpFunction()

        HttpRequestMessage<Optional<String>> request = createHttpRequestMessage("/parameters/loggerBinding")
        HttpResponseMessage responseMessage = function
                .route(request, new DefaultExecutionContext())

        expect:
        responseMessage.statusCode == HttpStatus.OK.code

        cleanup:
        function.close()
    }

    void "test string body"() {

        given:
        AzureHttpFunction function = new AzureHttpFunction()
        HttpResponseMessage responseMessage = function
                .request(HttpMethod.POST, "/parameters/stringBody")
                .body("Foo")
                .header(HttpHeaders.CONTENT_TYPE, "text/plain")
                .invoke()

        expect:
        responseMessage.statusCode == HttpStatus.OK.code
        responseMessage.getHeader(HttpHeaders.CONTENT_TYPE) == MediaType.APPLICATION_JSON
        responseMessage.body == 'Hello Foo'

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
        responseMessage.body == 'Hello Foo'
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
        responseMessage.body == json

        cleanup:
        function.close()
    }

    void "test JSON POJO body - invalid JSON"() {
        given:
        AzureHttpFunction function = new AzureHttpFunction()
        String json = '{"name":"bar","age":30'
        HttpResponseMessage responseMessage = function
                .request(HttpMethod.POST, "/parameters/jsonBody")
                .body(json)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .invoke()

        expect:
        responseMessage.statusCode == HttpStatus.BAD_REQUEST.code
        responseMessage.body.contains("Error decoding JSON stream for type")

        cleanup:
        function.close()
    }

    void "test JSON POJO body with no @Body binds to arguments"() {
        given:
        AzureHttpFunction function = new AzureHttpFunction()
        String json = '{"name":"bar","age":20}'
        HttpResponseMessage responseMessage = function
                .request(HttpMethod.POST, "/parameters/jsonBodySpread")
                .body(json)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .invoke()

        expect:
        responseMessage.statusCode == HttpStatus.OK.code
        responseMessage.getHeader(HttpHeaders.CONTENT_TYPE) == MediaType.APPLICATION_JSON
        responseMessage.body == json

        cleanup:
        function.close()
    }

    void "full Micronaut request and response"() {
        given:
        String json = '{"name":"bar","age":20}'
        AzureHttpFunction function = new AzureHttpFunction()
        HttpResponseMessage responseMessage = function
                .request(HttpMethod.POST, "/parameters/fullRequest")
                .body(json)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .invoke()

        expect:
        responseMessage.statusCode == HttpStatus.OK.code
        responseMessage.getHeader(HttpHeaders.CONTENT_TYPE) == MediaType.APPLICATION_JSON
        responseMessage.body == json
        responseMessage.getHeader("Foo") == 'Bar'

        cleanup:
        function.close()
    }

    void "full Micronaut request and response - invalid JSON"() {
        given:
        AzureHttpFunction function = new AzureHttpFunction()
        String json = '{"name":"bar","age":20'
        HttpResponseMessage responseMessage = function
                .request(HttpMethod.POST, "/parameters/fullRequest")
                .body(json)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .invoke()

        expect:
        responseMessage.statusCode == HttpStatus.BAD_REQUEST.code
        responseMessage.body.contains("Error decoding request body")

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


    private HttpRequestMessage<Optional<String>> createHttpRequestMessage(String path) {
        return new HttpRequestMessage<Optional<String>>() {
            @Override
            URI getUri() {
                return URI.create(path);
            }

            @Override
            HttpMethod getHttpMethod() {
                return HttpMethod.GET;
            }

            @Override
            Map<String, String> getHeaders() {
                return Collections.emptyMap();
            }

            @Override
            Map<String, String> getQueryParameters() {
                return Collections.emptyMap();
            }

            @Override
            Optional<String> getBody() {
                return Optional.empty();
            }

            @Override
            HttpResponseMessage.Builder createResponseBuilder(com.microsoft.azure.functions.HttpStatus status) {
                return new ResponseBuilder().status(status);
            }

            @Override
            HttpResponseMessage.Builder createResponseBuilder(HttpStatusType status) {
                return new ResponseBuilder().status(status);
            }
        }
    }
}
