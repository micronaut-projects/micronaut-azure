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
        return HttpResponse.ok("Good job!")
            .header(HttpHeaders.CONTENT_LENGTH, "9")
            .header("Transfer-Encoding", AzureHttpFunction.TRANSFER_ENCODING_CHUNKED);
    }

    @Produces(MediaType.TEXT_PLAIN)
    @Get("/content-length")
    public HttpResponse<String> contentLengthOnly(HttpRequest<?> request) {
        return HttpResponse.ok("Good job!")
            .header(HttpHeaders.CONTENT_LENGTH, "9");
    }

    @Produces(MediaType.TEXT_PLAIN)
    @Get("/transfer-encoding-non-chunked")
    public HttpResponse<String> transferEncodingNonChunked(HttpRequest<?> request) {
        return HttpResponse.ok("Good job!")
            .header(HttpHeaders.CONTENT_LENGTH, "9")
            .header("Transfer-Encoding", "gzip");
    }
}
