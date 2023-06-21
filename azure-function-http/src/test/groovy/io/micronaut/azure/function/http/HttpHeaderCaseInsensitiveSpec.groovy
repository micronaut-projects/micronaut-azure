package io.micronaut.azure.function.http

import com.microsoft.azure.functions.HttpMethod
import com.microsoft.azure.functions.HttpResponseMessage
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import spock.lang.Issue
import spock.lang.Specification

class HttpHeaderCaseInsensitiveSpec extends Specification {

    @Issue("https://github.com/micronaut-projects/micronaut-azure/issues/183")
    void "verify Http header names are case insensitive"(Map<String, String> headers) {
        given:
        AzureHttpFunction function = new AzureHttpFunction()

        when: 'send  request with an Http Header Host in lowercase'
        def responseMessage = retrieve(function, headers)

        then:
        responseMessage.statusCode == HttpStatus.OK.code

        and:
        responseMessage.body == 'Accept: text/plain Turbo-frame: messages'

        cleanup:
        function.close()

        where:
        headers <<  [
                [
                        "accept": MediaType.TEXT_PLAIN,
                        "Turbo-Frame": "messages"
                ],
                [
                        "Accept": MediaType.TEXT_PLAIN,
                        "Turbo-FRAME": "messages"
                ],
        ]
    }

    private static HttpResponseMessage retrieve(AzureHttpFunction function, Map<String, String> headersMap) {
        HttpRequestMessageBuilder<?> builder = function.request(HttpMethod.GET, "/headers/echo")
        for (String headerName : headersMap.keySet()) {
            builder.header(headerName, headersMap.get(headerName))
        }
        TestUtils.invoke(function, builder)
    }
}
