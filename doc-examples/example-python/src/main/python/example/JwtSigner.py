from com.azure.security.keyvault.keys.cryptography.models import SignatureAlgorithm
from jakarta.inject import Singleton
from micronaut.azure.secretmanager.signing import KeyVaultKeySigner


# tag::class[]
@Singleton
class JwtSigner:

    def __init__(self, signer: KeyVaultKeySigner):  # <1>
        self.signer = signer

    def sign(self, payload: bytes) -> bytes:
        return self.signer.sign("jwt-signing-key", payload)  # <2>
    # end::class[]

    def sign_with_algorithm(self, payload: bytes) -> bytes:
        # tag::algorithm[]
        signature = self.signer.sign("jwt-signing-key", SignatureAlgorithm.RS256, payload)
        # end::algorithm[]
        return signature
    # tag::class[]
# end::class[]
