package example

import com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm
import io.micronaut.azure.secretmanager.signing.KeyVaultKeySigner
import jakarta.inject.Singleton

// tag::class[]
@Singleton
class JwtSigner(private val signer: KeyVaultKeySigner) { // <1>

    fun sign(payload: ByteArray): ByteArray {
        return signer.sign("jwt-signing-key", payload) // <2>
    }
    // end::class[]

    fun signWithAlgorithm(payload: ByteArray): ByteArray {
        // tag::algorithm[]
        val signature = signer.sign("jwt-signing-key", SignatureAlgorithm.RS256, payload)
        // end::algorithm[]
        return signature
    }
    // tag::class[]
}
// end::class[]
