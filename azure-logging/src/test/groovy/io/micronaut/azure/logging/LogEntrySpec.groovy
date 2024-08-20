package io.micronaut.azure.logging

import io.micronaut.context.annotation.Property
import io.micronaut.serde.ObjectMapper
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

import static io.micronaut.core.util.StringUtils.FALSE

@MicronautTest
@Property(name = 'spec.name', value = 'LogEntrySpec')
@Property(name = 'azure.logging.enabled', value = FALSE)
class LogEntrySpec extends Specification {

    @Inject
    ObjectMapper objectMapper

    void 'check serialization'() {
        expect:
        objectMapper.writeValueAsString(new LogEntry('data', 123, 'source', 'subject')) ==
                '{"Data":"data","EventTimestamp":123,"Source":"source","Subject":"subject"}'
    }
}
