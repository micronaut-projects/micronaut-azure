package io.micronaut.azure.function.http.test

import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification

import jakarta.inject.Inject

@MicronautTest
@Property(name = "spec.name", value = "AzureFunctionEmbeddedServerSpec")
@Property(name = "micronaut.server.context-path", value =  "/api")
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

    @Requires(property = 'spec.name', value = 'AzureFunctionEmbeddedServerSpec')
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
    }
}
