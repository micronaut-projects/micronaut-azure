package example.support;

import com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm;
import io.micronaut.azure.secretmanager.signing.KeyVaultKeySigner;
import jakarta.inject.Singleton;
import org.jspecify.annotations.NonNull;

import java.nio.charset.StandardCharsets;

/**
 * Stands in for the Key Vault backed signer, which needs a vault, and records the key and algorithm in the signature.
 */
@Singleton
public class FakeKeyVaultKeySigner implements KeyVaultKeySigner {

    public static final SignatureAlgorithm DEFAULT_ALGORITHM = SignatureAlgorithm.ES256;

    @Override
    public byte[] sign(@NonNull String keyName, @NonNull SignatureAlgorithm algorithm, @NonNull byte[] data) {
        return (keyName + ":" + algorithm + ":" + new String(data, StandardCharsets.UTF_8)).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public byte[] sign(@NonNull String keyName, @NonNull byte[] data) {
        return sign(keyName, DEFAULT_ALGORITHM, data);
    }
}
