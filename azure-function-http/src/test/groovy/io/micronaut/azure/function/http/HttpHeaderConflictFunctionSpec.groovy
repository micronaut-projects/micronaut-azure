package io.micronaut.azure.function.http

import com.microsoft.azure.functions.HttpMethod
import com.microsoft.azure.functions.HttpResponseMessage
import io.micronaut.http.HttpStatus
import spock.lang.Issue
import spock.lang.Specification;

class HttpHeaderConflictFunctionSpec extends Specification {

    @Issue("https://github.com/micronaut-projects/micronaut-azure/issues/696")
    void "verify managed response framing headers are not returned in context Azure Function"() {
        given:
        AzureHttpFunction function = new AzureHttpFunction()

        when:
        HttpResponseMessage responseMessage = retrieve(function)

        then:
        responseMessage.statusCode == HttpStatus.OK.code
        responseMessage.body == 'Good job!'

        and:
        responseMessage.getHeader('Transfer-Encoding') == null
        responseMessage.getHeader('Content-Length') == null

        cleanup:
        function.close()
    }

    void "verify Content-Length is preserved when Transfer-Encoding is not present"() {
        given:
        AzureHttpFunction function = new AzureHttpFunction()

        when:
        HttpResponseMessage responseMessage = retrieve(function, "/headers/content-length")

        then:
        responseMessage.statusCode == HttpStatus.OK.code
        responseMessage.body == 'Good job!'

        and:
        responseMessage.getHeader('Transfer-Encoding') == null
        responseMessage.getHeader('Content-Length') == '9'

        cleanup:
        function.close()
    }

    void "verify Content-Length is preserved when Transfer-Encoding is not chunked"() {
        given:
        AzureHttpFunction function = new AzureHttpFunction()

        when:
        HttpResponseMessage responseMessage = retrieve(function, "/headers/transfer-encoding-non-chunked")

        then:
        responseMessage.statusCode == HttpStatus.OK.code
        responseMessage.body == 'Good job!'

        and:
        responseMessage.getHeader('Transfer-Encoding') == null
        responseMessage.getHeader('Content-Length') == '9'

        cleanup:
        function.close()
    }

    private static HttpResponseMessage retrieve(AzureHttpFunction function) {
        retrieve(function, "/headers/echo2")
    }

    private static HttpResponseMessage retrieve(AzureHttpFunction function, String path) {
        HttpRequestMessageBuilder<?> builder = function.request(HttpMethod.GET, path)
        builder.invoke()
    }
}
