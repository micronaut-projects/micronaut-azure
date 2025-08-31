package io.micronaut.azure.cosmos.client

import io.micronaut.core.util.StringUtils
import spock.lang.Specification

class NettySslConfiguratorSpec extends Specification {

    void 'test init sets system property'() {
        given:
        def config = new NettySslConfiguration("HTTPS")
        def configurator = new NettySslConfigurator()
        System.clearProperty("io.netty.handler.ssl.defaultEndpointVerificationAlgorithm")
        when:
        configurator.initNettySslConfiguration(config)
        then:
        System.getProperty("io.netty.handler.ssl.defaultEndpointVerificationAlgorithm") == "HTTPS"
    }

    void 'test init does not override existing system property'() {
        given:
        def config = new NettySslConfiguration("HTTPS")
        def configurator = new NettySslConfigurator()
        System.setProperty("io.netty.handler.ssl.defaultEndpointVerificationAlgorithm", "ExistingValue")
        when:
        configurator.initNettySslConfiguration(config)
        then:
        System.getProperty("io.netty.handler.ssl.defaultEndpointVerificationAlgorithm") == "ExistingValue"
    }

    void 'test init does nothing when config is null'() {
        given:
        def configurator = new NettySslConfigurator()
        System.clearProperty("io.netty.handler.ssl.defaultEndpointVerificationAlgorithm")
        when:
        configurator.initNettySslConfiguration(null)
        then:
        StringUtils.isEmpty(System.getProperty("io.netty.handler.ssl.defaultEndpointVerificationAlgorithm"))
    }
}
