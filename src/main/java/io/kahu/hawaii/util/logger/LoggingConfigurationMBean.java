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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jolokia.jmx.JsonMBean;

@JsonMBean
public class LoggingConfigurationMBean {
    private final LogManager logManager;
    private final LogManagerConfiguration logManagerConfiguration;

    public LoggingConfigurationMBean(LogManager logManager, LogManagerConfiguration logManagerConfiguration) {
        this.logManager = logManager;
        this.logManagerConfiguration = logManagerConfiguration;
    }

    public void setComplexityThreshold(String name, int complexityThreshold) {
        logManagerConfiguration.getOrCreateLoggingConfiguration(name).setComplexityThreshold(complexityThreshold);
    }

    public void enableCompleteCallLogging(String name) {
        logManagerConfiguration.getOrCreateLoggingConfiguration(name).enableCompleteCallLogging();
    }

    public void resetLoggingConfiguration(String name) {
        logManagerConfiguration.resetLoggingConfiguration(name);
    }

    public String getLoggerCatagories() throws JSONException {
        return getLoggerCatagories(true, true);
    }

    public String getLoggerCatagories(boolean filterHawaii, boolean filterConfigured) throws JSONException {
        Enumeration<?> currentLoggers = org.apache.log4j.LogManager.getCurrentLoggers();

        Map<String, Logger> loggers = new HashMap<>();

        while (currentLoggers.hasMoreElements()) {
            Object nextElement = currentLoggers.nextElement();
            if (nextElement instanceof Logger) {
                Logger logger = (Logger) nextElement;
                loggers.put(logger.getName(), logger);
            }
        }

        List<String> keys = new ArrayList<>(loggers.keySet());
        Collections.sort(keys);

        JSONArray jsonLoggers = new JSONArray();
        for (String key : keys) {
            Logger logger = loggers.get(key);

            String loggerName = logger.getName();
            boolean isConfigured = logger.getLevel() != null;

            if (filterHawaii && loggerName.startsWith("hawaii") || !filterHawaii) {
                if (filterConfigured && isConfigured || !filterConfigured) {
                    JSONObject jsonLogger = new JSONObject();
                    jsonLogger.put("category", loggerName);
                    jsonLogger.put("level", logger.getEffectiveLevel());
                    jsonLogger.put("is_configured", isConfigured);
                    jsonLoggers.put(jsonLogger);
                }
            }
        }
        return jsonLoggers.toString(2);
    }

    public void setLogLevel(String category, String level) {
        if ("null".equalsIgnoreCase(level)) {
            level = null;
        }
        logManager.setLevel(new SimpleLoggerName(category), level);
    }

    public void setLogLevels(String source) throws JSONException {
        if (source.startsWith("'")) {
            source = source.substring(1, source.length() - 1);
        }
        JSONArray jsonLoggers = new JSONArray(source);
        for (int i = 0; i < jsonLoggers.length(); i++) {
            JSONObject jsonLogger = jsonLoggers.getJSONObject(i);
            String category = jsonLogger.getString("category");
            String level = jsonLogger.getString("level");
            setLogLevel(category, level);
        }
    }

}
