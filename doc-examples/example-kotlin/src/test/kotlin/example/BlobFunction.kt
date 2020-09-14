package example

import com.microsoft.azure.functions.annotation.BlobOutput
import com.microsoft.azure.functions.annotation.BlobTrigger
import com.microsoft.azure.functions.annotation.FunctionName
import com.microsoft.azure.functions.annotation.StorageAccount
import io.micronaut.azure.function.AzureFunction
import io.micronaut.context.event.ApplicationEvent
import io.micronaut.context.event.ApplicationEventPublisher
import javax.inject.Inject

class BlobFunction : AzureFunction() { // <1>
    @Inject
    lateinit var eventPublisher : ApplicationEventPublisher // <2>

    @FunctionName("copy")
    @StorageAccount("AzureWebJobsStorage")
    @BlobOutput(name = "\$return", path = "samples-output-java/{name}") // <3>
    fun copy(@BlobTrigger(name = "blob", path = "samples-input-java/{name}") content: String): String {
        eventPublisher.publishEvent(BlobEvent(content)) // <4>
        return content
    }

    class BlobEvent(content: String) : ApplicationEvent(content)
}