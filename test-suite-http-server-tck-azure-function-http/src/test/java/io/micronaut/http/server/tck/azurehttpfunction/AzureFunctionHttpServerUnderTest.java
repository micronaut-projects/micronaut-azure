package io.micronaut.http.server.tck.azurehttpfunction;

import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import io.micronaut.azure.function.AzureFunction;
import io.micronaut.azure.function.http.DefaultExecutionContext;
import io.micronaut.context.ApplicationContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.http.tck.ServerUnderTest;
import io.micronaut.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@SuppressWarnings("java:S2187") // Suppress because despite its name, this is not a Test
public class AzureFunctionHttpServerUnderTest implements ServerUnderTest {

    private static final Logger LOG = LoggerFactory.getLogger(AzureFunctionHttpServerUnderTest.class);

    Function function;

    public AzureFunctionHttpServerUnderTest(@NonNull Map<String, Object> properties) {
        properties.put("endpoints.health.service-ready-indicator-enabled", StringUtils.FALSE);
        properties.put("endpoints.refresh.enabled", StringUtils.FALSE);
        this.function = new Function(AzureFunction.defaultApplicationContextBuilder().properties(properties));
    }

    @Override
    public <I, O> HttpResponse<O> exchange(HttpRequest<I> request, Argument<O> bodyType) {
        HttpRequestMessage<Optional<String>> requestMessage = AzureRequestEventFactory.create(request, function.getApplicationContext().getBean(JsonMapper.class));
        HttpResponseMessage responseMessage = function.route(requestMessage, new DefaultExecutionContext());
        HttpResponse<O> response = new HttpResponseMessageAdapter<>(responseMessage, function.getApplicationContext().getBean(ConversionService.class), HEADERS_USED_IN_TEST_SUITE);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Response status: {}", response.getStatus());
        }
        if (response.getStatus().getCode() >= 400) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Response body: {}", response.getBody(String.class));
            }
            throw new HttpClientResponseException("error " + response.getStatus().getReason() + " (" + response.getStatus().getCode() + ")", response);
        }
        return response;
    }

    @Override
    public <I, O, E> HttpResponse<O> exchange(HttpRequest<I> request, Argument<O> bodyType, Argument<E> errorType) {
        return exchange(request, bodyType);
    }

    @Override
    public ApplicationContext getApplicationContext() {
        return function.getApplicationContext();
    }

    @Override
    public Optional<Integer> getPort() {
        return Optional.of(1234);
    }

    @Override
    public void close() throws IOException {
        function.close();
    }

    private final static Set<String> HEADERS_USED_IN_TEST_SUITE = Set.of(
        "X-Test-Filter",
        "X-Captured-Remote-Address"
    );

}
