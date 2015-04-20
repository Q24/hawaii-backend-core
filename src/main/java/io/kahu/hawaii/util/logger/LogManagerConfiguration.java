/**
 * Copyright 2015 Q24
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.kahu.hawaii.util.logger;

import java.util.HashMap;
import java.util.Map;

public class LogManagerConfiguration {
    public static final String DEFAULT = "DEFAULT";
    private final LoggingConfiguration defaultLoggingConfig;

    private final Map<String, LoggingConfiguration> loggingConfigurations = new HashMap<String, LoggingConfiguration>();

    public LogManagerConfiguration(LoggingConfiguration defaultLoggingConfig) {
        this.defaultLoggingConfig = defaultLoggingConfig;
    }

    public LoggingConfiguration getLoggingConfiguration(String key) {
        if (DEFAULT.equals(key)) {
            return defaultLoggingConfig;
        }
        return loggingConfigurations.get(key);
    }

    public LoggingConfiguration getOrCreateLoggingConfiguration(String name) {
        LoggingConfiguration loggingConfiguration = getLoggingConfiguration(name);
        if (loggingConfiguration == null) {
            loggingConfiguration = getLoggingConfiguration(LogManagerConfiguration.DEFAULT).clone();
            addLoggingConfiguration(name, loggingConfiguration);
        }
        return loggingConfiguration;
    }

    public void addLoggingConfiguration(String name, LoggingConfiguration loggingConfiguration) {
        this.loggingConfigurations.put(name, loggingConfiguration);
    }

    public LoggingConfiguration resetLoggingConfiguration(String name) {
        return this.loggingConfigurations.remove(name);
    }
}
