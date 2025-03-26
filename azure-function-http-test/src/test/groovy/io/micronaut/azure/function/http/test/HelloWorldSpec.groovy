package io.micronaut.azure.function.http.test

import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.client.BlockingHttpClient
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@Property(name = "spec.name", value = "HelloWorldTest")
@MicronautTest
class HelloWorldSpec extends Specification {
    @Inject
    @Client("/")
    HttpClient httpClient

    void testHelloWorld() {
        given:
        BlockingHttpClient client = httpClient.toBlocking()
        when:
        String json = client.retrieve("/hello/world", String.class)
        then:
        noExceptionThrown()
        '{"message":"Hello World"}' == json
    }

    @Requires(property = "spec.name", value = "HelloWorldTest")
    @Controller("/hello")
    static class HelloWorldController {
        @Get("/world")
        String index() {
            "{\"message\":\"Hello World\"}"
        }
    }
}
