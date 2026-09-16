package example

import example.support.BlobEventListener
import spock.lang.Specification

class BlobFunctionSpec extends Specification {

    void "copies the blob and publishes an event"() {
        given:
        BlobFunction function = new BlobFunction()

        when:
        String result = function.copy("blob content")

        then:
        result == "blob content"
        function.applicationContext.getBean(BlobEventListener).received == ["blob content"]

        cleanup:
        function.close()
    }
}
