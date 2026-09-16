package example

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

@MicronautTest
class JwtSignerTest {

    @Inject
    lateinit var jwtSigner: JwtSigner

    @Test
    fun signsWithTheDefaultAlgorithm() {
        val signature = jwtSigner.sign("payload".toByteArray())

        assertEquals("jwt-signing-key:ES256:payload", String(signature))
    }

    @Test
    fun signsWithAnExplicitAlgorithm() {
        val signature = jwtSigner.signWithAlgorithm("payload".toByteArray())

        assertEquals("jwt-signing-key:RS256:payload", String(signature))
    }
}
