from typing import Annotated

from jakarta.inject import Inject
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test

from example.BlobEventListener import BlobEventListener
from example.BlobFunction import BlobFunction


@MicronautTest
class BlobFunctionTest:
    function: Annotated[BlobFunction, Inject]
    listener: Annotated[BlobEventListener, Inject]

    @Test
    def test_copies_the_blob_and_publishes_an_event(self) -> None:
        result = self.function.copy("blob content")

        assert result == "blob content"
        assert self.listener.received == ["blob content"]
