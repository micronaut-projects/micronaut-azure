package io.micronaut.azure.function.http;

import com.microsoft.azure.functions.*;
import io.micronaut.core.io.Writable;
import io.micronaut.http.*;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import io.micronaut.http.cookie.Cookie;

import java.io.IOException;
import java.util.Optional;


@Controller("/parameters")
public class ParametersController {

    @Get("/uri/{name}")
    String uriParam(String name) {
        return "Hello " + name;
    }

    @Get("/query")
    String queryValue(@QueryValue("q") String name) {
        return "Hello " + name;
    }

    @Get("/allParams")
    String allParams(HttpParameters parameters) {
        return "Hello " + parameters.get("name") + " " + parameters.get("age", int.class).orElse(null);
    }

    @Get("/header")
    String headerValue(@Header(HttpHeaders.CONTENT_TYPE) String contentType) {
        return "Hello " + contentType;
    }

    @Get("/cookies")
    io.micronaut.http.HttpResponse<String> cookies(@CookieValue String myCookie) {
        return io.micronaut.http.HttpResponse.ok(myCookie)
                .cookie(Cookie.of("foo", "bar").httpOnly(true).domain("http://foo.com"));
    }

    @Get("/reqAndRes")
    HttpResponseMessage requestAndResponse(
            HttpRequestMessage<Optional<String>> request) throws IOException {
        HttpResponseMessage.Builder builder = request.createResponseBuilder(com.microsoft.azure.functions.HttpStatus.OK);
        builder.header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN);
        builder.status(HttpStatusType.custom(HttpStatus.ACCEPTED.getCode()));
        builder.body("Good");
        return builder.build();
    }

    @Get("/executionContext")
    @Status(HttpStatus.OK)
    void executionContextBinding(
            ExecutionContext executionContext) throws IOException {
    }

    @Get("/traceContext")
    @Status(HttpStatus.OK)
    void traceContextBinding(
            TraceContext traceContext) throws IOException {
    }

    @Get("/loggerBinding")
    @Status(HttpStatus.OK)
    void loggerBinding(
            java.util.logging.Logger logger) throws IOException {
    }

    @Post("/stringBody")
    @Consumes("text/plain")
    String stringBody(@Body String body) {
        return "Hello " + body;
    }

    @Post("/bytesBody")
    @Consumes("text/plain")
    String bytesBody(@Body byte[] body) {
        return "Hello " + new String(body);
    }

    @Post(value = "/jsonBody", processes = "application/json")
    Person jsonBody(@Body Person body) {
        return body;
    }

    @Post(value = "/jsonBodySpread", processes = "application/json")
    Person jsonBody(String name, int age) {
        return new Person(name, age);
    }

    @Post(value = "/fullRequest", processes = "application/json")
    io.micronaut.http.HttpResponse<Person> fullReq(io.micronaut.http.HttpRequest<Person> request) {
        final Person person = request.getBody().orElseThrow(() -> new RuntimeException("No body"));
        final MutableHttpResponse<Person> response = io.micronaut.http.HttpResponse.ok(person);
        response.header("Foo", "Bar");
        return response;
    }

    @Post(value = "/writable", processes = "text/plain")
    @Header(name = "Foo", value = "Bar")
    @Status(HttpStatus.CREATED)
    Writable fullReq(@Body String text) {
        return out -> out.append("Hello ").append(text);
    }


}