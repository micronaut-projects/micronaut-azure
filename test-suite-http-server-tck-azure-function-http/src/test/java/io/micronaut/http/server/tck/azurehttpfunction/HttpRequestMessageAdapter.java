package io.micronaut.http.server.tck.azurehttpfunction;

import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.HttpStatusType;

import java.net.URI;
import java.util.Map;

public class HttpRequestMessageAdapter implements HttpRequestMessage {
    @Override
    public URI getUri() {
        return null;
    }

    @Override
    public HttpMethod getHttpMethod() {
        return null;
    }

    @Override
    public Map<String, String> getHeaders() {
        return null;
    }

    @Override
    public Map<String, String> getQueryParameters() {
        return null;
    }

    @Override
    public Object getBody() {
        return null;
    }

    @Override
    public HttpResponseMessage.Builder createResponseBuilder(HttpStatus status) {
        return null;
    }

    @Override
    public HttpResponseMessage.Builder createResponseBuilder(HttpStatusType status) {
        return null;
    }
}
