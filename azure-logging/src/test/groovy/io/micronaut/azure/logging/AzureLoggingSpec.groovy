package io.micronaut.azure.logging

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.spi.LoggingEvent
import ch.qos.logback.core.encoder.LayoutWrappingEncoder
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
import spock.lang.Stepwise
import spock.util.concurrent.PollingConditions

import static ch.qos.logback.classic.Level.INFO

@MicronautTest(startApplication = false)
@Property(name = 'spec.name', value = 'AzureLoggingSpec')
@Property(name = 'azure.logging.dataCollectionEndpoint', value = 'test-dataCollectionEndpoint-from-application-config')
@Property(name = 'azure.logging.ruleId', value = 'test-ruleId-from-application-config')
@Property(name = 'azure.logging.streamName', value = 'test-streamName-from-application-config')
class AzureLoggingSpec extends Specification {
    private final LoggerContext context = new LoggerContext()
    private final PatternLayout layout = new PatternLayout(context: context, pattern: '[%thread] %level %logger{20} - %msg%n%xThrowable')
    private final LayoutWrappingEncoder encoder = new LayoutWrappingEncoder(layout: layout)
    private final AzureAppender appender = new AzureAppender(context: context, encoder: encoder)

    @Inject
    ClientWrapper clientWrapper

    @Inject
    ApplicationEventPublisher<ServerStartupEvent> eventPublisher

    @Inject
    ApplicationConfiguration applicationConfiguration

    void setup() {
        layout.start()
        encoder.start()

        var config = Stub(ApplicationConfiguration) {
            getName() >> Optional.of('my-awesome-app')
        }

        var instance = Mock(EmbeddedServer)
        instance.getHost() >> 'testHost'

        new AzureLoggingClient(config, clientWrapper)
                .onApplicationEvent(new ServerStartupEvent(instance))
    }

    void cleanup() {
        layout.stop()
        encoder.stop()
        appender.stop()
        AzureLoggingClient.destroy()
    }

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


    void 'test error queue size less then 0'() {
        when:
        appender.queueSize = -1
        appender.start()

        then:
        context.statusManager.copyOfStatusList.find { it.message == 'Queue size must be greater than zero' }
    }

    void 'test error queue size equal to 0'() {
        when:
        appender.queueSize = 0
        appender.start()

        then:
        context.statusManager.copyOfStatusList.find {
            it.message == 'Queue size of zero is deprecated, use a size of one to indicate synchronous processing'
        }
    }

    void 'test error max batch size less or equal to 0'() {
        when:
        appender.maxBatchSize = 0
        appender.start()

        then:
        context.statusManager.copyOfStatusList.find {
            it.message == 'Max Batch size must be greater than zero'
        }
    }

    void 'test error publish period less or equal to 0'() {
        when:
        appender.queueSize = 100
        appender.publishPeriod = 0
        appender.start()

        then:
        context.statusManager.copyOfStatusList.find {
            it.message == 'Publish period must be greater than zero'
        }
    }

    void 'encoder not set'() {
        when:
        appender.queueSize = 100
        appender.publishPeriod = 100
        appender.encoder = null
        appender.start()

        then:
        context.statusManager.copyOfStatusList.find {
            it.message == 'No encoder set for the appender named [null].'
        }
    }

    void 'register multiple emergency appender'() {
        given:
        var mockAppender = new ListAppender(name: 'MockAppender')

        when:
        appender.queueSize = 100
        appender.publishPeriod = 101
        appender.encoder = new LayoutWrappingEncoder()
        appender.addAppender mockAppender
        appender.addAppender mockAppender
        var statuses = context.statusManager.copyOfStatusList

        then:
        statuses.find { it.message == 'One and only one appender may be attached to AzureAppender' }
        statuses.find { it.message == 'Ignoring additional appender named [MockAppender]' }
        appender.getAppender 'MockAppender'
        !appender.getAppender('NotExistingOne')
        appender.isAttached(mockAppender)
        appender.encoder
        appender.queueSize == 100
        appender.publishPeriod == 101

        appender.detachAndStopAllAppenders()
        !appender.isAttached(mockAppender)
    }

    void 'detach emergency appender by name'() {
        when:
        appender.queueSize = 100
        appender.publishPeriod = 100
        appender.encoder = new LayoutWrappingEncoder()
        appender.addAppender new ListAppender(name: 'MockAppender')

        then:
        appender.detachAppender 'MockAppender'
        !appender.detachAppender('NotExistingOne')
    }

    void 'detach emergency appender by instance'() {
        when:
        def mockAppender = new ListAppender()
        appender.queueSize = 100
        appender.publishPeriod = 100
        appender.encoder = new LayoutWrappingEncoder()
        appender.addAppender(mockAppender)

        then:
        appender.detachAppender(mockAppender)
        !appender.detachAppender(mockAppender)
    }

    void 'try to create iterator for emergency appender'() {
        when:
        def mockAppender = new ListAppender()
        appender.queueSize = 100
        appender.publishPeriod = 100
        appender.setEncoder(new LayoutWrappingEncoder())
        appender.addAppender(mockAppender)
        appender.iteratorForAppenders()

        then:
        thrown(UnsupportedOperationException)
    }

    void 'custom subject, type and and source of log'() {
        given:
        String testSubject = 'testSubject'
        String testSource = 'testSource'
        String testMessage = 'testMessage'
        LoggingEvent event = createEvent('name', INFO, testMessage, System.currentTimeMillis())

        when:
        appender.subject = testSubject
        appender.source = testSource
        appender.start()
        appender.doAppend event

        then:
        appender.source == testSource
        appender.subject == testSubject
        new PollingConditions(timeout: 10, initialDelay: 1.5, factor: 1.25).eventually {
            clientWrapper.logsList.size() == 1
        }
        clientWrapper.logsList[0][0].source == testSource
        clientWrapper.logsList[0][0].subject == testSubject
    }

    private static LoggingEvent createEvent(String name, Level level, String message, Long time) {
        LoggingEvent event = new LoggingEvent(loggerName: name, level: level, message: message)
        if (time) {
            event.timeStamp = time
        }
        return event
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

        void close() {
            logsList.clear()
        }
    }
}
