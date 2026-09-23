package example

import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest
class JwtSignerSpec extends Specification {

    @Inject
    JwtSigner jwtSigner

    void "signs with the default algorithm"() {
        expect:
        new String(jwtSigner.sign("payload".bytes)) == "jwt-signing-key:ES256:payload"
    }

    void "signs with an explicit algorithm"() {
        expect:
        new String(jwtSigner.signWithAlgorithm("payload".bytes)) == "jwt-signing-key:RS256:payload"
    }
}
