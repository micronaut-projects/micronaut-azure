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

import com.azure.core.credential.TokenCredential;
import com.azure.monitor.ingestion.LogsIngestionClient;
import com.azure.monitor.ingestion.LogsIngestionClientBuilder;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;

/**
 * Default implementation.
 *
 * @since 5.6
 */
@Internal
class DefaultClientWrapper implements ClientWrapper {

    private final LogsIngestionClient client;
    private final String ruleId;
    private final String streamName;

    DefaultClientWrapper(@NonNull TokenCredential tokenCredential,
                         @NonNull AzureLoggingConfigurationProperties loggingConfig) {
        client = new LogsIngestionClientBuilder()
            .endpoint(loggingConfig.getDataCollectionEndpoint())
            .credential(tokenCredential)
            .buildClient();
        ruleId = loggingConfig.getRuleId();
        streamName = loggingConfig.getStreamName();
    }

    @Override
    public void upload(@NonNull Iterable<Object> logs) {
        client.upload(ruleId, streamName, logs);
    }

    @Override
    public void close() {
        client.close();
    }
}
