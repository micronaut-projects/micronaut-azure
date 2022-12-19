package io.micronaut.http.server.tck.azurehttpfunction;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.server.tck.ServerUnderTest;
import io.micronaut.http.server.tck.ServerUnderTestProvider;
import java.util.Map;

public class AzureFunctionHttpServerUnderTestProvider implements ServerUnderTestProvider {
    @Override
    @NonNull
    public ServerUnderTest getServer(@NonNull Map<String, Object> properties) {
        return new AzureFunctionHttpServerUnderTest(properties);
    }
}
