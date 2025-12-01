package io.micronaut.http.server.tck.azurehttpfunction;

import org.jspecify.annotations.NonNull;
import io.micronaut.http.tck.ServerUnderTest;
import io.micronaut.http.tck.ServerUnderTestProvider;

import java.util.Map;

public class AzureFunctionHttpServerUnderTestProvider implements ServerUnderTestProvider {
    @Override
    @NonNull
    public ServerUnderTest getServer(@NonNull Map<String, Object> properties) {
        return new AzureFunctionHttpServerUnderTest(properties);
    }
}
