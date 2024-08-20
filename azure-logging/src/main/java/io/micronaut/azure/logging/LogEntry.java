/*
 * Copyright 2017-2024 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.azure.logging;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Internal;
import io.micronaut.serde.annotation.Serdeable;

/**
 * Contains the logging data sent to Azure. Property names are capitalized
 * because they're used as the column names in the Log Analytics workspace
 * table that users will configure.
 *
 * @param data           the Logback event serialized as a String by an {@link ch.qos.logback.core.encoder.Encoder}
 * @param eventTimestamp the Logback event timestamp
 * @param source         the source of the log event; can be specified in the
 *                       appender configuration and defaults to the host of the server
 * @param subject        the subject of the log event; can be specified in the appender
 *                       configuration and defaults to the application name
 * @since 5.6
 */
@Serdeable
@Internal
public record LogEntry(@JsonProperty(value = "Data") String data,
                       @JsonProperty(value = "EventTimestamp") long eventTimestamp,
                       @JsonProperty(value = "Source") String source,
                       @JsonProperty(value = "Subject") String subject) {
}
