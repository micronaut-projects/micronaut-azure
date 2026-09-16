package example.support

import com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm
import io.micronaut.azure.secretmanager.signing.KeyVaultKeySigner
import jakarta.inject.Singleton

/**
 * Stands in for the Key Vault backed signer, which needs a vault, and records the key and algorithm in the signature.
 */
@Singleton
class FakeKeyVaultKeySigner : KeyVaultKeySigner {

    override fun sign(keyName: String, algorithm: SignatureAlgorithm, data: ByteArray): ByteArray =
        "$keyName:$algorithm:${String(data)}".toByteArray()

    override fun sign(keyName: String, data: ByteArray): ByteArray = sign(keyName, SignatureAlgorithm.ES256, data)
}
