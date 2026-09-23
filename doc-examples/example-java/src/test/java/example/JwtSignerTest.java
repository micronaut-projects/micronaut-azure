package example;

import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest
class JwtSignerTest {

    @Inject
    JwtSigner jwtSigner;

    @Test
    void signsWithTheDefaultAlgorithm() {
        byte[] signature = jwtSigner.sign("payload".getBytes(StandardCharsets.UTF_8));

        assertEquals("jwt-signing-key:ES256:payload", new String(signature, StandardCharsets.UTF_8));
    }

    @Test
    void signsWithAnExplicitAlgorithm() {
        byte[] signature = jwtSigner.signWithAlgorithm("payload".getBytes(StandardCharsets.UTF_8));

        assertEquals("jwt-signing-key:RS256:payload", new String(signature, StandardCharsets.UTF_8));
    }
}
