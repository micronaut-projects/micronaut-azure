package io.micronaut.azure.function.http;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.context.env.Environment;
import io.micronaut.function.executor.FunctionInitializer;
import io.micronaut.servlet.http.ServletExchange;
import io.micronaut.servlet.http.ServletHttpHandler;
import io.netty.util.internal.MacAddressUtil;
import io.netty.util.internal.PlatformDependent;

import java.util.Optional;

/**
 * This class can be used as a base class for Azure HTTP functions that wish to route requests
 * to classes annotated with {@link io.micronaut.http.annotation.Controller} and the others annotations in the
 * {@link io.micronaut.http.annotation} package.
 *
 * <p>To use this class you should define a new function that subclasses this class and then override the {@link #invoke(HttpRequestMessage, ExecutionContext)} method to customize the function mapping as per the Azure documentation. For example the following definition will route all requests to the function:</p>
 *
 * <pre>{@code
 *    @FunctionName("myFunction")
 *    @Override
 *    public HttpResponseMessage invoke(
 *       @HttpTrigger(name = "req",
 *                    route = "{*url}", // catch all route
 *                    authLevel = AuthorizationLevel.ANONYMOUS)
 *       HttpRequestMessage<Optional<byte[]>> request,
 *       ExecutionContext executionContext) {
 *
 *    }
 *
 * }</pre>
 *
 * @author graemerocher
 * @since 1.0.0
 */
public class AzureHttpFunction extends FunctionInitializer {
    static {
        byte[] bestMacAddr = new byte[8];
        PlatformDependent.threadLocalRandom().nextBytes(bestMacAddr);
        System.setProperty("io.netty.machineId", MacAddressUtil.formatAddress(bestMacAddr));
    }

    private final ServletHttpHandler<HttpRequestMessage<Optional<byte[]>>, HttpResponseMessage> httpHandler;

    /**
     * Default constructor.
     */
    public AzureHttpFunction() {
        this.httpHandler = new ServletHttpHandler<HttpRequestMessage<Optional<byte[]>>, HttpResponseMessage>(applicationContext) {
            @Override
            public boolean isRunning() {
                return applicationContext.isRunning();
            }

            @Override
            protected ServletExchange<HttpRequestMessage<Optional<byte[]>>, HttpResponseMessage> createExchange(HttpRequestMessage<Optional<byte[]>> request, HttpResponseMessage response) {
                throw new UnsupportedOperationException("Creating the exchange directly is not supported");
            }
        };

        Runtime.getRuntime().addShutdownHook(
                new Thread(httpHandler::close)
        );
    }

    /**
     * Entry point for Azure functions written in Micronaut.
     *
     * @param request The request
     * @param executionContext The execution context
     * @return THe response message
     */
    public HttpResponseMessage invoke(
            HttpRequestMessage<Optional<byte[]>> request,
            ExecutionContext executionContext) {
        AzureFunctionHttpRequest<?> azureFunctionHttpRequest =
                new AzureFunctionHttpRequest<>(
                        request,
                        this.httpHandler.getMediaTypeCodecRegistry(),
                        executionContext
                );

        ServletExchange<HttpRequestMessage<Optional<byte[]>>, HttpResponseMessage> exchange =
                httpHandler.exchange(azureFunctionHttpRequest);

        return exchange.getResponse().getNativeResponse();
    }

    @NonNull
    @Override
    protected ApplicationContextBuilder newApplicationContextBuilder() {
        ApplicationContextBuilder builder = super.newApplicationContextBuilder();
        builder.environments(Environment.AZURE);
        builder.deduceEnvironment(false);
        return builder;
    }
}
