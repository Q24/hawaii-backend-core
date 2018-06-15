/**
 * Copyright 2014-2018 Q24
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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Value object for configuration for the LogManager and LoggingContext.
 *
 * @author ErnstJan.Plugge
 */

public class LoggingConfiguration {
    private String[] tracingFields = new String[] {};
    private String[] skippedLocationClasses = new String[] {};

    private int complexityThreshold = 5;

    private int maxInfoRequestBodySize = 40;
    private int maxDebugRequestBodySize = 10000;
    private int maxInfoResponseBodySize = 40;
    private int maxDebugResponseBodySize = 10000;

    private int maxOutInfoRequestBodySize = 40;
    private int maxOutDebugRequestBodySize = 10000;
    private int maxOutInfoResponseBodySize = 40;
    private int maxOutDebugResponseBodySize = 10000;

    private Set<String> urlFields = Collections.emptySet();
    private Set<String> parameterFields = Collections.emptySet();
    private Set<String> headerFields = Collections.emptySet();
    private Set<String> bodyFields = Collections.emptySet();
    private Set<String> passwordParameters = Collections.emptySet();
    private Pattern[] bodyPasswordPatterns = new Pattern[] {};
    private String[] bodyPasswordFields;

    public String[] getTracingFields() {
        return tracingFields;
    }

    public void setTracingFields(final String[] tracingFields) {
        this.tracingFields = tracingFields;
    }

    public String[] getSkippedLocationClasses() {
        return skippedLocationClasses;
    }

    public void setSkippedLocationClasses(final String[] skippedLocationClasses) {
        this.skippedLocationClasses = skippedLocationClasses;
    }

    public int getComplexityThreshold() {
        return complexityThreshold;
    }

    public void setComplexityThreshold(final int complexityThreshold) {
        this.complexityThreshold = complexityThreshold;
    }

    public int getMaxInfoRequestBodySize() {
        return maxInfoRequestBodySize;
    }

    public void setMaxInfoRequestBodySize(final int maxInfoRequestBodySize) {
        this.maxInfoRequestBodySize = maxInfoRequestBodySize;
    }

    public int getMaxDebugRequestBodySize() {
        return maxDebugRequestBodySize;
    }

    public void setMaxDebugRequestBodySize(final int maxDebugRequestBodySize) {
        this.maxDebugRequestBodySize = maxDebugRequestBodySize;
    }

    public int getMaxInfoResponseBodySize() {
        return maxInfoResponseBodySize;
    }

    public void setMaxInfoResponseBodySize(final int maxInfoResponseBodySize) {
        this.maxInfoResponseBodySize = maxInfoResponseBodySize;
    }

    public int getMaxDebugResponseBodySize() {
        return maxDebugResponseBodySize;
    }

    public void setMaxDebugResponseBodySize(final int maxDebugResponseBodySize) {
        this.maxDebugResponseBodySize = maxDebugResponseBodySize;
    }

    public int getMaxOutInfoRequestBodySize() {
        return maxOutInfoRequestBodySize;
    }

    public void setMaxOutInfoRequestBodySize(final int maxOutInfoRequestBodySize) {
        this.maxOutInfoRequestBodySize = maxOutInfoRequestBodySize;
    }

    public int getMaxOutDebugRequestBodySize() {
        return maxOutDebugRequestBodySize;
    }

    public void setMaxOutDebugRequestBodySize(final int maxOutDebugRequestBodySize) {
        this.maxOutDebugRequestBodySize = maxOutDebugRequestBodySize;
    }

    public int getMaxOutInfoResponseBodySize() {
        return maxOutInfoResponseBodySize;
    }

    public void setMaxOutInfoResponseBodySize(final int maxOutInfoResponseBodySize) {
        this.maxOutInfoResponseBodySize = maxOutInfoResponseBodySize;
    }

    public int getMaxOutDebugResponseBodySize() {
        return maxOutDebugResponseBodySize;
    }

    public void setMaxOutDebugResponseBodySize(final int maxOutDebugResponseBodySize) {
        this.maxOutDebugResponseBodySize = maxOutDebugResponseBodySize;
    }

    public Set<String> getUrlFields() {
        return urlFields;
    }

    public void setUrlFields(final String[] urlFields) {
        this.urlFields = new HashSet<>();
        for (String s : urlFields) {
            this.urlFields.add(s);
        }
    }

    public Set<String> getParameterFields() {
        return parameterFields;
    }

    public void setParameterFields(final String[] parameterFields) {
        this.parameterFields = new HashSet<>();
        for (String s : parameterFields) {
            this.parameterFields.add(s);
        }
    }

    public Set<String> getHeaderFields() {
        return headerFields;
    }

    public void setHeaderFields(final String[] headerFields) {
        this.headerFields = new HashSet<>();
        for (String s : headerFields) {
            this.headerFields.add(s);
        }
    }

    public Set<String> getBodyFields() {
        return bodyFields;
    }

    public void setBodyFields(final String[] bodyFields) {
        this.bodyFields = new HashSet<>();
        for (String s : bodyFields) {
            this.bodyFields.add(s);
        }
    }

    public Set<String> getPasswordParameters() {
        return passwordParameters;
    }

    public void setPasswordParameters(final String[] passwordParameters) {
        this.passwordParameters = new HashSet<>();
        for (String s : passwordParameters) {
            this.passwordParameters.add(s);
        }
    }

    public Pattern[] getBodyPasswordPatterns() {
        return bodyPasswordPatterns;
    }

    public void setBodyPasswordPatterns(final String[] bodyPasswordPatterns) {
        this.bodyPasswordPatterns = new Pattern[bodyPasswordPatterns.length];
        for (int i = 0; i < bodyPasswordPatterns.length; i++) {
            this.bodyPasswordPatterns[i] = Pattern.compile(bodyPasswordPatterns[i], Pattern.DOTALL);
        }
    }

    public void setBodyPasswordFields(final String[] bodyPasswordFields) {
        this.bodyPasswordFields = bodyPasswordFields;
    }

    public String[] getBodyPasswordFields() {
        return bodyPasswordFields;
    }

    @Override
    public LoggingConfiguration clone() {
        LoggingConfiguration clone = new LoggingConfiguration();
        clone.bodyFields = bodyFields;
        clone.bodyPasswordPatterns = bodyPasswordPatterns;
        clone.bodyPasswordFields = bodyPasswordFields;
        clone.complexityThreshold = complexityThreshold;
        clone.headerFields = new HashSet<>(headerFields);

        clone.maxInfoRequestBodySize = maxInfoRequestBodySize;
        clone.maxDebugRequestBodySize = maxDebugRequestBodySize;
        clone.maxOutInfoRequestBodySize = maxOutInfoRequestBodySize;
        clone.maxOutDebugRequestBodySize = maxOutDebugRequestBodySize;

        clone.maxInfoResponseBodySize = maxInfoResponseBodySize;
        clone.maxDebugResponseBodySize = maxDebugResponseBodySize;
        clone.maxOutInfoResponseBodySize = maxOutInfoResponseBodySize;
        clone.maxOutDebugResponseBodySize = maxOutDebugResponseBodySize;

        clone.parameterFields = new HashSet<>(parameterFields);
        clone.passwordParameters = new HashSet<>(passwordParameters);
        clone.skippedLocationClasses = skippedLocationClasses;
        clone.tracingFields = tracingFields;
        clone.urlFields = new HashSet<>(urlFields);

        return clone;
    }

    public void enableCompleteCallLogging() {
        setMaxDebugRequestBodySize(Integer.MAX_VALUE);
        setMaxDebugResponseBodySize(Integer.MAX_VALUE);

        setMaxOutDebugRequestBodySize(Integer.MAX_VALUE);
        setMaxOutDebugResponseBodySize(Integer.MAX_VALUE);
    }
}
