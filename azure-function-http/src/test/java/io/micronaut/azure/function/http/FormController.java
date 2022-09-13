package io.micronaut.azure.function.http;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;

@Controller("/form")
public class FormController {

    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    @Post("/form-url-encoded")
    public String formUrlEncoded(@Body InputMessage inputMessage) {
        return "Output: " + inputMessage.getMessage();
    }

    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_PLAIN)
    @Post("/form-url-encoded-nested-attribute")
    public String formUrlEncodedNestedAttribute(@Body("message") String inputMessage) {
        return "Output: " + inputMessage;
    }
}

@Introspected
class InputMessage {
    private String message;

    public InputMessage() {
    }

    public InputMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setName(String message) {
        this.message = message;
    }
}

