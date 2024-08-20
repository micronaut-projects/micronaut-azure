package io.micronaut.azure.logging

import io.micronaut.context.ApplicationContext
import io.micronaut.context.exceptions.NoSuchBeanException
import spock.lang.Specification

class AzureLoggingConfigurationSpec extends Specification {

    void 'check configuration'() {
        given:

        String dataCollectionEndpoint = UUID.randomUUID()
        String ruleId = UUID.randomUUID()
        String streamName = UUID.randomUUID()

        var ctx = ApplicationContext.run([
                'spec.name'                           : 'AzureLoggingConfigurationSpec',
                'azure.logging.enabled'               : 'false',
                'azure.logging.dataCollectionEndpoint': dataCollectionEndpoint,
                'azure.logging.ruleId'                : ruleId,
                'azure.logging.streamName'            : streamName
        ])
        var config = ctx.getBean(AzureLoggingConfigurationProperties)

        expect:
        config.dataCollectionEndpoint == dataCollectionEndpoint
        config.ruleId == ruleId
        config.streamName == streamName

        cleanup:
        ctx.close()
    }

    void 'no LoggingClient bean when disabled'() {
        given:
        var ctx = ApplicationContext.run([
                'spec.name'            : 'AzureLoggingConfigurationSpec',
                'azure.logging.enabled': 'false'
        ])

        when:
        ctx.getBean AzureLoggingClient

        then:
        thrown NoSuchBeanException

        cleanup:
        ctx.close()
    }
}
