package io.micronaut.azure.function.http.test

import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Consumes
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Issue
import spock.lang.PendingFeature
import spock.lang.Specification

import jakarta.inject.Inject

@MicronautTest
class AzureFunctionEmbeddedServerSpec extends Specification {
    @Inject
    @Client('/')
    HttpClient client

    void 'test invoke function via server'() {
        when:
        def result = client.toBlocking().retrieve('/api/test')

        then:
        result == 'good'
    }

    void 'test invoke post via server'() {
        when:
        def result = client.toBlocking().retrieve(HttpRequest.POST('/api/test', "body")
                .contentType(MediaType.TEXT_PLAIN), String)

        then:
        result == 'goodbody'
    }

    @PendingFeature
    @Issue("https://github.com/micronaut-projects/micronaut-aws/issues/1410")
    // this copies a test we have in micronaut-aws, MicronautLambdaHandlerSpec
    // this is failing in AzureFunctionEmbeddedServer at line 261, with java.lang.IllegalStateException: STREAMED
    // or is my test just wrong?
    void 'test invoke form-url-encoded post via server'() {
        when:
        def result = client.toBlocking().retrieve(HttpRequest.POST('/api/test/nested-attribute', "message=World")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED), String)

        then:
        result == '{"message":"Hello World"}'
    }

    @Controller('/test')
    static class TestController {
        @Get(value = '/', produces = MediaType.TEXT_PLAIN)
        String test() {
            return 'good'
        }

        @Post(value = '/', processes = MediaType.TEXT_PLAIN)
        String test(@Body String body) {
            return 'good' + body
        }

        @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
        @Post("/nested-attribute")
        String save(@Body("message") String value) {
            "{\"message\":\"Hello ${value}\"}";
        }
    }
}
