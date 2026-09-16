from typing import Annotated

from jakarta.inject import Inject
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test

from example.JwtSigner import JwtSigner


@MicronautTest
class JwtSignerTest:
    jwt_signer: Annotated[JwtSigner, Inject]

    @Test
    def test_signs_with_the_default_algorithm(self) -> None:
        signature = self.jwt_signer.sign(b"payload")

        assert bytes(signature).decode() == "jwt-signing-key:ES256:payload"

    @Test
    def test_signs_with_an_explicit_algorithm(self) -> None:
        signature = self.jwt_signer.sign_with_algorithm(b"payload")

        assert bytes(signature).decode() == "jwt-signing-key:RS256:payload"
