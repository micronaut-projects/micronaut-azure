package example;

import com.microsoft.azure.functions.annotation.BlobOutput;
import com.microsoft.azure.functions.annotation.BlobTrigger;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.StorageAccount;
import io.micronaut.azure.function.AzureFunction;
import io.micronaut.context.event.ApplicationEvent;
import io.micronaut.context.event.ApplicationEventPublisher;

import javax.inject.Inject;

public class BlobFunction extends AzureFunction { // <1>
    @Inject
    ApplicationEventPublisher eventPublisher; // <2>

    @FunctionName("copy")
    @StorageAccount("AzureWebJobsStorage")
    @BlobOutput(name = "$return", path = "samples-output-java/{name}") // <3>
    public String copy(@BlobTrigger(
            name = "blob",
            path = "samples-input-java/{name}") String content) {
        eventPublisher.publishEvent(new BlobEvent(content)); // <4>
        return content;
    }

    public static class BlobEvent extends ApplicationEvent {
        public BlobEvent(String content) {
            super(content);
        }
    }
}
