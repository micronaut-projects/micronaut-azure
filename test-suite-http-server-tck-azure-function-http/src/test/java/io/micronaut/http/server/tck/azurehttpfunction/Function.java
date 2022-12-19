package io.micronaut.http.server.tck.azurehttpfunction;

import io.micronaut.azure.function.http.AzureHttpFunction;
import io.micronaut.context.ApplicationContextBuilder;

public class Function extends AzureHttpFunction {

    public Function(ApplicationContextBuilder applicationContextBuilder) {
        super(applicationContextBuilder);
    }
}
