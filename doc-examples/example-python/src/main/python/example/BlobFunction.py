from dataclasses import dataclass
from typing import Annotated

from com.microsoft.azure.functions.annotation import BlobOutput, BlobTrigger, FunctionName, StorageAccount
from jakarta.inject import Inject
from micronaut.context.event import ApplicationEventPublisher


@dataclass
class BlobEvent:
    content: str


class BlobFunction:  # <1>
    event_publisher: Annotated[ApplicationEventPublisher[BlobEvent], Inject]  # <2>

    @FunctionName("copy")
    @StorageAccount("AzureWebJobsStorage")
    @BlobOutput(name="$return", path="samples-output-java/{name}")  # <3>
    def copy(self, content: Annotated[str, BlobTrigger(name="blob", path="samples-input-java/{name}")]) -> str:
        self.event_publisher.publishEvent(BlobEvent(content))  # <4>
        return content
