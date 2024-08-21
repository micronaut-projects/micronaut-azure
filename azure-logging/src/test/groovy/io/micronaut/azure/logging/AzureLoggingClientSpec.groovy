package io.micronaut.azure.logging

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Property
import spock.lang.Specification

import static io.micronaut.context.env.Environment.AZURE

@Property(name = 'spec.name', value = 'LoggingClientSpec')
class AzureLoggingClientSpec extends Specification {

    void 'test it does not load when globally disabled'() {
        given:
        var context = ApplicationContext.run(['azure.logging.enabled': 'false'], AZURE)

        expect:
        !context.containsBean(AzureLoggingClient)
    }
}
