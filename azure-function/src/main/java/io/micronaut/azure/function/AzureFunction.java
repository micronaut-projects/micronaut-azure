package io.micronaut.azure.function;

import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.context.env.Environment;
import io.micronaut.function.executor.FunctionInitializer;

/**
 * A base Azure function class that sets up the Azure environment and preferred configuration.
 *
 * @author graemerocher
 * @since 1.0.0
 */
public abstract class AzureFunction extends FunctionInitializer {
    @Override
    protected ApplicationContextBuilder newApplicationContextBuilder() {
        ApplicationContextBuilder builder = super.newApplicationContextBuilder();
        builder.environments(Environment.AZURE);
        builder.deduceEnvironment(false);
        return builder;
    }
}
