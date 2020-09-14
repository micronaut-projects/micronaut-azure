package io.micronaut.azure.function

import spock.lang.Specification

class AzureFunctionSpec extends Specification {

    void "AzureFunction implements AutoCloseable"() {
        expect:
        new FooFunction() instanceof AutoCloseable
    }

    static class FooFunction extends AzureFunction {

    }
}
