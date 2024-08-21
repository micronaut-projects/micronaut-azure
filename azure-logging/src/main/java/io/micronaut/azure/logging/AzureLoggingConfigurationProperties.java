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

import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.env.Environment;

/**
 * Configuration for Azure logging.
 *
 * @since 5.6
 */
@ConfigurationProperties(AzureLoggingConfigurationProperties.PREFIX)
@BootstrapContextCompatible
public class AzureLoggingConfigurationProperties {

    /**
     * Prefix.
     */
    public static final String PREFIX = Environment.AZURE + ".logging";

    private boolean enabled = true;
    private String dataCollectionEndpoint;
    private String ruleId;
    private String streamName;

    /**
     * @param enabled true if enabled
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * @return true if enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param dataCollectionEndpoint data collection endpoint URL
     */
    public void setDataCollectionEndpoint(String dataCollectionEndpoint) {
        this.dataCollectionEndpoint = dataCollectionEndpoint;
    }

    /**
     * @return data collection endpoint URL
     */
    public String getDataCollectionEndpoint() {
        return dataCollectionEndpoint;
    }

    /**
     * @param ruleId data collection rule id
     */
    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    /**
     * @return data collection rule id
     */
    public String getRuleId() {
        return ruleId;
    }

    /**
     * @param streamName stream name
     */
    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    /**
     * @return stream name
     */
    public String getStreamName() {
        return streamName;
    }
}
