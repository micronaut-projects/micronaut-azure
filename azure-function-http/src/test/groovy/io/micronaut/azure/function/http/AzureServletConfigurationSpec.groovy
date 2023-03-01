package io.micronaut.azure.function.http

import io.micronaut.context.ApplicationContext
import io.micronaut.core.util.StringUtils
import spock.lang.Specification

class AzureServletConfigurationSpec extends Specification {

    void "async file serving defaults to true"() {
        given:
        def ctx = ApplicationContext.run()
        def cfg = ctx.getBean(AzureServletConfiguration)

        expect:
        cfg.asyncFileServingEnabled
    }

    void "async file serving can be disabled"() {
        given:
        def prop = AzureServletConfiguration.PREFIX + '.async-file-serving-enabled'
        def ctx = ApplicationContext.run((prop): StringUtils.FALSE)
        def cfg = ctx.getBean(AzureServletConfiguration)

        expect:
        !cfg.asyncFileServingEnabled
    }
}
