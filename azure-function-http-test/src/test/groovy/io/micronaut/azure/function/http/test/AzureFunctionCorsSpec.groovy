package io.micronaut.azure.function.http.test

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Error
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.test.support.TestPropertyProvider
import spock.lang.Specification
import jakarta.inject.Inject

import static io.micronaut.http.HttpHeaders.*

@MicronautTest
class AzureFunctionCorsSpec extends Specification implements TestPropertyProvider {

    @Inject @Client("/") HttpClient client

    void "test non cors request"() {
        when:
        HttpResponse<?> response = client.toBlocking().exchange('/api/cors/test')
        Set<String> headerNames = response.getHeaders().names()

        then:
        response.status == HttpStatus.NO_CONTENT
        response.contentLength == -1
        !headerNames.contains(ACCESS_CONTROL_MAX_AGE)
        !headerNames.contains(VARY)
        !headerNames.contains(ACCESS_CONTROL_ALLOW_METHODS)
        !headerNames.contains(ACCESS_CONTROL_ALLOW_HEADERS)
        headerNames.size() == 2
        headerNames.contains(DATE)
        headerNames.contains(SERVER)
    }

    void "test cors request without configuration"() {
        given:
        HttpResponse<?> response = client.toBlocking().exchange(
                HttpRequest.GET('/api/cors/test')
                        .header(ORIGIN, 'fooBar.com')
        )

        when:
        Set<String> headerNames = response.headers.names()

        then:
        response.status == HttpStatus.NO_CONTENT
        !headerNames.contains(ACCESS_CONTROL_MAX_AGE)
        !headerNames.contains(VARY)
        !headerNames.contains(ACCESS_CONTROL_ALLOW_METHODS)
        !headerNames.contains(ACCESS_CONTROL_ALLOW_HEADERS)
        headerNames.size() == 2
        headerNames.contains(DATE)
        headerNames.contains(SERVER)
    }

    void "test cors request with a controller that returns map"() {
        given:
        HttpResponse<?> response = client.toBlocking().exchange(
                HttpRequest.GET('/api/cors/test/arbitrary')
                        .header(ORIGIN, 'foo.com')
        )

        when:
        Set<String> headerNames = response.headers.names()

        then:
        response.status == HttpStatus.OK
        response.header(ACCESS_CONTROL_ALLOW_ORIGIN) == 'foo.com'
        response.header(VARY) == ORIGIN
        !headerNames.contains(ACCESS_CONTROL_MAX_AGE)
        !headerNames.contains(ACCESS_CONTROL_ALLOW_HEADERS)
        !headerNames.contains(ACCESS_CONTROL_ALLOW_METHODS)
        !headerNames.contains(ACCESS_CONTROL_EXPOSE_HEADERS)
        response.header(ACCESS_CONTROL_ALLOW_CREDENTIALS) == 'true'
    }

    void "test cors request with controlled method"() {
        given:
        HttpResponse<?> response = client.toBlocking().exchange(
                HttpRequest.GET('/api/cors/test')
                        .header(ORIGIN, 'foo.com')
        )

        when:
        Set<String> headerNames = response.headers.names()

        then:
        response.status == HttpStatus.NO_CONTENT
        response.header(ACCESS_CONTROL_ALLOW_ORIGIN) == 'foo.com'
        response.header(VARY) == ORIGIN
        !headerNames.contains(ACCESS_CONTROL_MAX_AGE)
        !headerNames.contains(ACCESS_CONTROL_ALLOW_HEADERS)
        !headerNames.contains(ACCESS_CONTROL_ALLOW_METHODS)
        !headerNames.contains(ACCESS_CONTROL_EXPOSE_HEADERS)
        response.header(ACCESS_CONTROL_ALLOW_CREDENTIALS) == 'true'
    }

    void "test cors request with controlled headers"() {
        given:
        HttpResponse<?> response = client.toBlocking().exchange(
                HttpRequest.GET('/api/cors/test')
                        .header(ORIGIN, 'bar.com')
                        .header(ACCEPT, 'application/json')

        )

        when:
        Set<String> headerNames = response.headers.names()

        then:
        response.code() == HttpStatus.NO_CONTENT.code
        response.header(ACCESS_CONTROL_ALLOW_ORIGIN) == 'bar.com'
        response.header(VARY) == ORIGIN
        !headerNames.contains(ACCESS_CONTROL_MAX_AGE)
        !headerNames.contains(ACCESS_CONTROL_ALLOW_HEADERS)
        !headerNames.contains(ACCESS_CONTROL_ALLOW_METHODS)
        response.headers.getAll(ACCESS_CONTROL_EXPOSE_HEADERS) == ['x']
        !headerNames.contains(ACCESS_CONTROL_ALLOW_CREDENTIALS)
    }

    void "test cors request with invalid method"() {
        given:
        List<String> expected = [
                ALLOW,
                DATE,
                CONTENT_TYPE,
                CONTENT_LENGTH,
                SERVER
        ]
        when:
        client.toBlocking().exchange(
                HttpRequest.POST('/api/cors/test', [:])
                        .header(ORIGIN, 'foo.com')
        )

        then:
        HttpClientResponseException e = thrown()
        HttpResponse<?> response =  e.response

        when:
        Set<String> headerNames = response.headers.names()

        then:
        response.code() == HttpStatus.FORBIDDEN.code
        headerNames.size() == expected.size()
        expected.every {expectedHeaderName ->
            headerNames.any  { header -> header.equalsIgnoreCase(expectedHeaderName) }
        }
        MediaType.APPLICATION_JSON == response.header(CONTENT_TYPE)
        'HEAD,GET' == response.header(ALLOW)
    }

    void "test cors request with invalid header"() {
        given:
        HttpResponse<?> response = client.toBlocking().exchange(
                HttpRequest.GET('/api/cors/test')
                        .header(ORIGIN, 'bar.com')
                        .header(ACCESS_CONTROL_REQUEST_HEADERS, 'Foo, Accept')
        )

        expect: "it passes through because only preflight requests check allowed headers"
        response.code() == HttpStatus.NO_CONTENT.code
    }

    void "test preflight request with invalid header"() {
        when:
        client.toBlocking().exchange(
                HttpRequest.OPTIONS('/api/cors/test')
                        .header(ACCESS_CONTROL_REQUEST_METHOD, 'GET')
                        .header(ORIGIN, 'bar.com')
                        .header(ACCESS_CONTROL_REQUEST_HEADERS, 'Foo, Accept')
        )

        then:
        HttpClientResponseException e = thrown()

        and: "it fails because preflight requests check allowed headers"
        e.status == HttpStatus.FORBIDDEN
    }

    void "test preflight request with invalid method"() {
        when:
        client.toBlocking().exchange(
                HttpRequest.OPTIONS('/api/cors/test')
                        .header(ACCESS_CONTROL_REQUEST_METHOD, 'POST')
                        .header(ORIGIN, 'foo.com')

        )

        then:
        HttpClientResponseException e = thrown()
        e.status == HttpStatus.FORBIDDEN
    }

    void "test preflight request with controlled method"() {
        given:
        HttpResponse<?> response = client.toBlocking().exchange(
                HttpRequest.OPTIONS('/api/cors/test')
                        .header(ACCESS_CONTROL_REQUEST_METHOD, 'GET')
                        .header(ORIGIN, 'foo.com')
                        .header(ACCESS_CONTROL_REQUEST_HEADERS, 'Foo, Bar')
        )

        def headerNames = response.headers.names()

        expect:
        response.code() == HttpStatus.OK.code
        response.header(ACCESS_CONTROL_ALLOW_METHODS) == 'GET'
        response.headers.getAll(ACCESS_CONTROL_ALLOW_HEADERS) == ['Foo']
        !headerNames.contains(ACCESS_CONTROL_MAX_AGE)
        response.header(ACCESS_CONTROL_ALLOW_ORIGIN) == 'foo.com'
        response.header(VARY) == ORIGIN
        !headerNames.contains(ACCESS_CONTROL_EXPOSE_HEADERS)
        response.header(ACCESS_CONTROL_ALLOW_CREDENTIALS) == 'true'
    }

    void "test preflight request with controlled headers"() {
        given:
        HttpResponse<?> response = client.toBlocking().exchange(
                HttpRequest.OPTIONS('/api/cors/test')
                        .header(ACCESS_CONTROL_REQUEST_METHOD, 'GET')
                        .header(ORIGIN, 'bar.com')
                        .header(ACCESS_CONTROL_REQUEST_HEADERS, 'Accept')
        )

        def headerNames = response.headers.names()

        expect:
        response.code() == HttpStatus.OK.code
        response.header(ACCESS_CONTROL_ALLOW_METHODS) == 'GET'
        response.headers.getAll(ACCESS_CONTROL_ALLOW_HEADERS) == ['Accept']
        response.header(ACCESS_CONTROL_MAX_AGE) == '150'
        response.header(ACCESS_CONTROL_ALLOW_ORIGIN) == 'bar.com'
        response.header(VARY) == ORIGIN
        // azure doesn't support multi value headers 🤦‍♂️
        response.headers.getAll(ACCESS_CONTROL_EXPOSE_HEADERS) == ['x']
        !headerNames.contains(ACCESS_CONTROL_ALLOW_CREDENTIALS)
    }

    void "test control headers are applied to error response routes"() {
        when:
        client.toBlocking().exchange(
                HttpRequest.GET('/api/cors/test/error')
                        .header(ORIGIN, 'foo.com')
        )

        then:
        def ex = thrown(HttpClientResponseException)
        ex.response.status == HttpStatus.BAD_REQUEST
        ex.response.header(ACCESS_CONTROL_ALLOW_ORIGIN) == 'foo.com'
        ex.response.header(VARY) == ORIGIN
    }

    void "test control headers are applied to error responses with no handler"() {
        when:
        client.toBlocking().exchange(
                HttpRequest.GET('/api/cors/test/error-checked')
                        .header(ORIGIN, 'foo.com')
        )

        then:
        def ex = thrown(HttpClientResponseException)
        ex.response.status == HttpStatus.INTERNAL_SERVER_ERROR
        ex.response.header(ACCESS_CONTROL_ALLOW_ORIGIN) == 'foo.com'
        ex.response.header(VARY) == ORIGIN
    }

    void "test control headers are applied to http error responses"() {
        when:
        client.toBlocking().exchange(
                HttpRequest.GET('/api/cors/test/error-response')
                        .header(ORIGIN, 'foo.com')
        )

        then:
        def ex = thrown(HttpClientResponseException)
        ex.response.status == HttpStatus.BAD_REQUEST
        ex.response.header(ACCESS_CONTROL_ALLOW_ORIGIN) == 'foo.com'
        ex.response.headers.getAll(ACCESS_CONTROL_ALLOW_ORIGIN).size() == 1
        ex.response.header(VARY) == ORIGIN
    }

    @Override
    Map<String, String> getProperties() {
        ['micronaut.server.cors.enabled': "true",
         'micronaut.server.cors.configurations.foo.allowed-origins': 'foo.com',
         'micronaut.server.cors.configurations.foo.allowed-methods': 'GET',
         'micronaut.server.cors.configurations.foo.max-age': '-1',
         'micronaut.server.cors.configurations.bar.allowed-origins': 'bar.com',
         'micronaut.server.cors.configurations.bar.allowed-headers[0]': CONTENT_TYPE,
         'micronaut.server.cors.configurations.bar.allowed-headers[1]': ACCEPT,
         'micronaut.server.cors.configurations.bar.exposed-headers[0]': 'x',
         'micronaut.server.cors.configurations.bar.exposed-headers[1]': 'y',
         'micronaut.server.cors.configurations.bar.max-age': '150',
         'micronaut.server.cors.configurations.bar.allow-credentials': 'false',
         'micronaut.server.date-header': 'false']
    }

    @Controller('/cors/test')
    static class TestController {

        @Get
        HttpResponse index() {
            HttpResponse.noContent()
        }

        @Get('/arbitrary')
        Map arbitrary() {
            [some: 'data']
        }

        @Get("/error")
        String error() {
            throw new RuntimeException("error")
        }

        @Get("/error-checked")
        String errorChecked() {
            throw new IOException("error")
        }

        @Get("/error-response")
        HttpResponse errorResponse() {
            HttpResponse.badRequest()
        }

        @Error(exception = RuntimeException)
        HttpResponse onError() {
            HttpResponse.badRequest()
        }
    }
}

