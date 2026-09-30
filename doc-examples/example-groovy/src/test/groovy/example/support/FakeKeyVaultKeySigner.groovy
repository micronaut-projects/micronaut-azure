package example.support

import com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm
import io.micronaut.azure.secretmanager.signing.KeyVaultKeySigner
import jakarta.inject.Singleton
import org.jspecify.annotations.NonNull

/**
 * Stands in for the Key Vault backed signer, which needs a vault, and records the key and algorithm in the signature.
 */
@Singleton
class FakeKeyVaultKeySigner implements KeyVaultKeySigner {

    @Override
    byte[] sign(@NonNull String keyName, @NonNull SignatureAlgorithm algorithm, @NonNull byte[] data) {
        return "${keyName}:${algorithm}:${new String(data)}".toString().bytes
    }

    @Override
    byte[] sign(@NonNull String keyName, @NonNull byte[] data) {
        return sign(keyName, SignatureAlgorithm.ES256, data)
    }
}
