package example

import com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm
import io.micronaut.azure.secretmanager.signing.KeyVaultKeySigner
import jakarta.inject.Singleton

// tag::class[]
@Singleton
class JwtSigner {
    private final KeyVaultKeySigner signer

    JwtSigner(KeyVaultKeySigner signer) { // <1>
        this.signer = signer
    }

    byte[] sign(byte[] payload) {
        return signer.sign("jwt-signing-key", payload) // <2>
    }
    // end::class[]

    byte[] signWithAlgorithm(byte[] payload) {
        // tag::algorithm[]
        byte[] signature = signer.sign("jwt-signing-key", SignatureAlgorithm.RS256, payload)
        // end::algorithm[]
        return signature
    }
    // tag::class[]
}
// end::class[]
