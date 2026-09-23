from dataclasses import dataclass
from typing import Annotated

from com.microsoft.azure.functions.annotation import BlobOutput, BlobTrigger, FunctionName, StorageAccount
from jakarta.inject import Inject
from micronaut.context.event import ApplicationEventPublisher


@dataclass
class BlobEvent:
    content: str


# TODO(python): the Java class extends AzureFunction, whose constructor starts the application context and injects
# the function; a Python subclass compiles but is not injected (the constructor injects the Java adapter, not the
# Python object) and, created as a bean, lives in the second application context, see DISABLED_TESTS.md
class BlobFunction:  # <1>
    event_publisher: Annotated[ApplicationEventPublisher[BlobEvent], Inject]  # <2>

    @FunctionName("copy")
    @StorageAccount("AzureWebJobsStorage")
    @BlobOutput(name="$return", path="samples-output-java/{name}")  # <3>
    def copy(self, content: Annotated[str, BlobTrigger(name="blob", path="samples-input-java/{name}")]) -> str:
        self.event_publisher.publishEvent(BlobEvent(content))  # <4>
        return content
