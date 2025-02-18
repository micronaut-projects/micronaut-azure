package io.micronaut.azure.logging

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.core.read.ListAppender
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Replaces
import io.micronaut.context.annotation.Requires
import io.micronaut.context.event.ApplicationEventPublisher
import io.micronaut.runtime.ApplicationConfiguration
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.runtime.server.event.ServerStartupEvent
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

@MicronautTest
@Property(name = 'spec.name', value = 'AzureLoggingSpec')
@Property(name = 'azure.logging.dataCollectionEndpoint', value = 'test-dataCollectionEndpoint-from-application-config')
@Property(name = 'azure.logging.ruleId', value = 'test-ruleId-from-application-config')
@Property(name = 'azure.logging.streamName', value = 'test-streamName-from-application-config')
class AzureLoggingSpec extends Specification {

    @Inject
    ClientWrapper clientWrapper

    @Inject
    ApplicationEventPublisher<ServerStartupEvent> eventPublisher

    @Inject
    ApplicationConfiguration applicationConfiguration

    void 'test Azure logging'() {
        given:
        String logMessage = 'test logging'
        String testHost = 'testHost'
        var logger = LoggerFactory.getLogger(AzureLoggingSpec)
        PollingConditions conditions = new PollingConditions(timeout: 10, initialDelay: 1.5, factor: 1.25)
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory()

        MockClientWrapper mockWrapper = (MockClientWrapper) clientWrapper
        var logsList = mockWrapper.logsList

        ListAppender listAppender
        for (l in loggerContext.loggerList) {
            for (a in l.iteratorForAppenders()) {
                if (a.name == 'AZURE') {
                    listAppender = (ListAppender) a.getAppender('MOCK')
                }
            }
        }

        when:
        var instance = Mock(EmbeddedServer)
        1 * instance.getHost() >> testHost

        eventPublisher.publishEvent new ServerStartupEvent(instance)

        logger.info logMessage

        then:
        conditions.eventually {
            !logsList.isEmpty()
        }

        Collection<LogEntry> allEntries = []
        for (Iterable<Object> iterable in logsList) {
            allEntries.addAll iterable
        }

        allEntries.every { it.source == testHost }
        allEntries.find { it.subject == applicationConfiguration.name.get() }

        allEntries.find { it.data.contains('io.micronaut.context') }
        allEntries.find { it.data.contains('io.micronaut.azure.logging.AzureLoggingSpec') }
        allEntries.find { it.data.contains(logMessage) }
        allEntries.find { it.data.contains('Established active environments') }

        listAppender.list.isEmpty()
    }

    @Requires(property = 'spec.name', value = 'AzureLoggingSpec')
    @Singleton
    @Replaces(ClientWrapper)
    static class MockClientWrapper implements ClientWrapper {

        final List<Iterable<Object>> logsList = [].asSynchronized() as List<Iterable<Object>>

        void upload(Iterable<Object> logs) {
            synchronized (logsList) {
                logsList << logs
            }
        }

        void close() {}
    }
}
