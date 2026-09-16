from jakarta.inject import Singleton
from micronaut.context.event import ApplicationEventListener

from example.BlobFunction import BlobEvent


@Singleton
class BlobEventListener(ApplicationEventListener[BlobEvent]):
    received: list[str] = []

    def onApplicationEvent(self, event: BlobEvent) -> None:
        self.received = self.received + [event.content]
