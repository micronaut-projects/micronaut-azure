package io.micronaut.azure.function.http;

import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;

@Controller("/headers")
public class HeadersController {

    @Produces(MediaType.TEXT_PLAIN)
    @Get("/echo")
    public String index(HttpRequest<?> request) {
        return "Accept: " + request.getHeaders().get(HttpHeaders.ACCEPT) + " Turbo-frame: " + request.getHeaders().get("Turbo-Frame");
    }

    @Produces(MediaType.TEXT_PLAIN)
    @Get("/echo2")
    public HttpResponse<String> index2(HttpRequest<?> request) {
        return HttpResponse.ok("Good job!").header("Transfer-Encoding", "chunked");
    }
}
