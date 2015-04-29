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

import io.kahu.hawaii.util.call.Response;
import io.kahu.hawaii.util.call.statistics.QueueStatistic;
import io.kahu.hawaii.util.call.statistics.RequestStatistic;
import io.kahu.hawaii.util.exception.HawaiiException;
import io.kahu.hawaii.util.exception.ServerError;
import io.kahu.hawaii.util.exception.ServerException;
import io.kahu.hawaii.util.exception.ValidationException;
import io.kahu.hawaii.util.logger.LoggingContext.PopResource;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.http.HttpStatus;

public class DefaultLogManager implements LogManager {
    public static final String MASKED_PASSWORD = "********";

    private final Map<String, Logger> loggers;

    private String literalField = null;
    private final LogManagerConfiguration config;

    public DefaultLogManager(LogManagerConfiguration config) {
        this.loggers = new HashMap<String, Logger>();
        this.config = config;
    }

    // Private copy constructor for builder pattern, no logging configurations
    // (plural), caller is responsible to pass the appropriate configuration
    private DefaultLogManager(Map<String, Logger> loggers, LogManagerConfiguration config, String literalField) {
        this.loggers = loggers;
        this.config = config;
        this.literalField = literalField;
    }

    private Logger getLogger(LoggerName name) {
        assert (name != null);

        String loggerName = name.getName();
        Logger logger = loggers.get(loggerName);
        if (logger == null) {
            logger = Logger.getLogger(loggerName);
            loggers.put(loggerName, logger);
        }
        return logger;
    }

    public void addLogger(LoggerName loggerName, Logger logger) {
        loggers.put(loggerName.getName(), logger);
    }

    @Override
    public void error(HawaiiException e) {
        assert (e != null);

        // Horrible hack to prevent validation exceptions to give stacktraces
        // and be logged as errors. Needs changes in validations to fix for real
        if (e instanceof ValidationException) {
            info(e.getLoggerName(), e.getMessage());
        } else {
            error(e.getLoggerName(), e);
        }
    }

    public void storeCaller() {
        StackTraceElement[] trace = new Throwable().getStackTrace();
        int i = 3;
        boolean skip = true;
        while (skip && i < trace.length) {
            skip = false;
            for (String cls : getLoggingConfiguration().getSkippedLocationClasses()) {
                if (trace[i].getClassName().startsWith(cls)) {
                    skip = true;
                    i++;
                    break;
                }
            }
        }
        if (i < trace.length) {
            String location = trace[i].getClassName() + ":" + trace[i].getLineNumber();
            putContext("log.loc", location);
        }
    }

    @Override
    public void setLevel(LoggerName name, String level) {
        try {
            Level logLevel = null;
            if (level != null) {
                Field field = Level.class.getField(level);
                logLevel = (Level) field.get(Level.DEBUG);
            }
            getLogger(name).setLevel(logLevel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void audit(AuditTrail trail) {
        assert (trail != null);
        getLogger(CoreLoggers.AUDIT).info(trail.toString());
    }

    @Override
    public void audit(String message) {
        assert (message != null);
        getLogger(CoreLoggers.AUDIT).info(message);
    }

    @Override
    public void trace(LoggerName name, String message) {
        if (getLogger(name).isTraceEnabled()) {
            log(name, Level.TRACE, message, null, LogType.NORMAL);
        }
    }
    
    @Override
    public void trace(LoggerName name, String message, Throwable t) {
        if (getLogger(name).isTraceEnabled()) {
            log(name, Level.TRACE, message, t, LogType.NORMAL);
        }
    }

    
    @Override
    public void debug(LoggerName name, String message) {
        if (getLogger(name).isDebugEnabled()) {
            log(name, Level.DEBUG, message, null, LogType.NORMAL);
        }
    }

    @Override
    public void debug(LoggerName name, String message, Throwable t) {
        if (getLogger(name).isDebugEnabled()) {
            log(name, Level.DEBUG, message, t, LogType.NORMAL);
        }
    }

    @Override
    public void info(LoggerName name, String message) {
        if (getLogger(name).isInfoEnabled()) {
            log(name, Level.INFO, message, null, LogType.NORMAL);
        }
    }

    @Override
    public void info(LoggerName name, String message, Throwable t) {
        if (getLogger(name).isInfoEnabled()) {
            log(name, Level.INFO, message, t, LogType.NORMAL);
        }
    }

    @Override
    public void warn(LoggerName name, String message) {
        log(name, Level.WARN, message, null, LogType.NORMAL);
    }

    @Override
    public void warn(LoggerName name, String message, Throwable t) {
        log(name, Level.WARN, message, t, LogType.NORMAL);
    }

    @Override
    public void error(LoggerName name, String message) {
        log(name, Level.ERROR, message, null, LogType.NORMAL);
    }

    @Override
    public void error(LoggerName name, Throwable t) {
        log(name, Level.ERROR, null, t, LogType.NORMAL);
    }

    @Override
    public void error(LoggerName name, String message, Throwable t) {
        log(name, Level.ERROR, message, t, LogType.NORMAL);
    }

    @Override
    public void fatal(LoggerName name, String message) {
        log(name, Level.FATAL, message, null, LogType.NORMAL);
    }

    @Override
    public void fatal(LoggerName name, String message, Throwable t) {
        log(name, Level.FATAL, message, t, LogType.NORMAL);
    }

    @Override
    public DefaultLogManager literalField(String literalField) {
        return new DefaultLogManager(loggers, config, literalField);
    }

    // Make sure we never log any passwords
    void maskPasswords(LoggingConfiguration config) throws JSONException {
        // Look at all fields that may contain a URL, and mask passwords in
        // query string parameters
        for (String field : config.getUrlFields()) {
            Object value = getContext(field);
            if (value == null || value == JSONObject.NULL || value == JSONObject.EXPLICIT_NULL) {
                continue;
            }
            if (!(value instanceof String)) {
                // If it's not a string, just play it safe and replace the
                // entire parameter
                putContext(field, MASKED_PASSWORD);
                continue;
            }

            // It's a String so iterate over the QS parameters
            String url = (String) value;
            int p1 = url.indexOf('?');
            boolean changed = false;
            while (p1 >= 0) {
                int p2 = url.indexOf('=', p1 + 1);
                if (p2 < 0) {
                    break;
                }
                String qsParam = url.substring(p1 + 1, p2);
                int p3 = url.indexOf('&', p2 + 1);
                if (p3 < 0) {
                    p3 = url.length();
                }
                String qsValue = url.substring(p2 + 1, p3);
                if (config.getPasswordParameters().contains(qsParam)) {
                    qsValue = MASKED_PASSWORD;
                    url = url.substring(0, p2 + 1) + qsValue + url.substring(p3);
                    changed = true;
                }
                p1 = url.indexOf('&', p1 + 1);
            }
            if (changed) {
                putContext(field, url);
            }
        }

        // Next, look at all parameter objects, which are String -> List of
        // String mappings
        for (String field : config.getParameterFields()) {
            Object value = getContext(field);
            if (value == null || value == JSONObject.NULL || value == JSONObject.EXPLICIT_NULL) {
                continue;
            }
            if (!(value instanceof JSONObject)) {
                // If it's not a JSONObject, just play it safe and replace the
                // entire parameter
                putContext(field, MASKED_PASSWORD);
                continue;
            }

            // It's a JSONObject, so discover and nuke all possible password
            // fields
            JSONObject params = (JSONObject) value;
            boolean changed = false;
            for (String key : config.getPasswordParameters()) {
                if (params.has(key)) {
                    JSONArray a = new JSONArray();
                    a.put(MASKED_PASSWORD);
                    params.put(key, a);
                    changed = true;
                }
            }
            if (changed) {
                putContext(field, params);
            }
        }

        // Next, look at all request/response headers, which are stored as an
        // array of strings
        for (String field : config.getHeaderFields()) {
            Object value = getContext(field);
            if (value == null || value == JSONObject.NULL || value == JSONObject.EXPLICIT_NULL) {
                continue;
            }
            if (!(value instanceof JSONArray)) {
                // If it's not a JSONArray, just play it safe and replace the
                // entire parameter
                putContext(field, MASKED_PASSWORD);
                continue;
            }

            // It's a JSONArray, so discover and nuke all possible password
            // headers
            JSONArray headers = (JSONArray) value;
            boolean changed = false;
            for (String key : config.getPasswordParameters()) {
                for (int i = 0; i < headers.length(); i++) {
                    String header = headers.getString(i);
                    if (header.startsWith(key + ":")) {
                        headers.put(i, key + ": " + MASKED_PASSWORD);
                        changed = true;
                    }
                }
            }
            if (changed) {
                putContext(field, headers);
            }
        }

        // Finally, look at all body fields for password patterns
        for (String field : config.getBodyFields()) {
            Object value = getContext(field);
            if (value == null || value == JSONObject.NULL || value == JSONObject.EXPLICIT_NULL) {
                continue;
            }
            if (!(value instanceof String)) {
                // If it's not a string, just play it safe and replace the
                // entire parameter
                putContext(field, MASKED_PASSWORD);
                continue;
            }

            // It's a String, so apply all password patterns to mask the
            // passwords
            String body = (String) value;
            boolean changed = false;
            for (Pattern pattern : config.getBodyPasswordPatterns()) {
                Matcher m = pattern.matcher(body);
                int i = 0;
                while (m.find(i)) {
                    body = body.substring(0, m.start(1)) + MASKED_PASSWORD + body.substring(m.end(1));
                    i = m.end() - m.group(1).length() + MASKED_PASSWORD.length();
                    m = pattern.matcher(body);
                    changed = true;
                }
            }
            if (changed) {
                putContext(field, body);
            }
        }
    }

    private void log(LoggerName loggerName, Level level, String message, Throwable t, LogType logType) {
        Logger logger = getLogger(loggerName);
        if (!logger.isEnabledFor(level)) {
            return;
        }
        LoggingConfiguration config = getLoggingConfiguration();
        try (PopResource pop = LoggingContext.get().push()) {
            storeCaller();
            putContext("message", message);
            if (t != null) {
                putContext("exception.class", t.getClass().getName());
                putContext("exception.message", t.getMessage());
                try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw);) {
                    t.printStackTrace(pw);
                    String trace = sw.toString();
                    putContext("exception.trace", trace);
                } catch (IOException cant_happen) {
                    putContext("exception.trace", "Unable to generate");
                }
            }

            try {
                maskPasswords(getLoggingConfiguration());
            } catch (JSONException cant_happen) {
                //
            }

            String field = literalField;
            if (field == null && t != null) {
                field = "exception.trace";
            }
            if (field == null && message != null && message.length() > 0) {
                field = "message";
            }

            MDC.put("logtype", logType);
            String formatted = LoggingContext.get().formatFields(field, config.getTracingFields(), config.getComplexityThreshold());
            logger.log(level, formatted);
            MDC.remove("logtype");
        }
    }

    @Override
    public void logIncomingCallStart(String type, String body, JSONObject params) {
        boolean largeBody = false;

        putContext("tx.type", type);
        LoggingConfiguration config = getLoggingConfiguration();

        String ppbody = prettyPrintJson(body);

        try (PopResource pop = pushContext()) {
            if (params != null && params.length() != 0) {
                putContext("tx.request.params", params);
            }
            if (ppbody != null) {
                String s = ppbody;
                if (s.length() > config.getMaxInfoRequestBodySize()) {
                    s = s.substring(0, config.getMaxInfoRequestBodySize()) + "[...]";
                    largeBody = true;
                }
                putContext("tx.request.body", s);
            }
            log(CoreLoggers.SERVER, Level.INFO, null, null, LogType.START);
        }

        if (largeBody) {
            try (PopResource pop = pushContext()) {
                String s = ppbody;
                if (s.length() > config.getMaxDebugRequestBodySize()) {
                    s = s.substring(0, config.getMaxDebugRequestBodySize()) + "[...]";
                }
                putContext("tx.request.body", s);
                literalField("tx.request.body").log(CoreLoggers.SERVER, Level.DEBUG, null, null, LogType.START);
            }
        }
    }

    @Override
    public void logIncomingCallEnd(Throwable t) {
        HawaiiException exception = null;
        if (t instanceof HawaiiException) {
            exception = (HawaiiException) t;
        } else {
            exception = new ServerException(ServerError.UNEXPECTED_EXCEPTION, t);
        }

        putContext("tx.result", (exception instanceof ServerException) ? 2 : 1);
        putContext("tx.response.status", exception.getStatus().value());
        putContext("tx.duration", getContextDuration());

        try (PopResource pop = pushContext()) {
            putContext("tx.response.exception", exception.getClass().getName());
            putContext("tx.response.message", exception.getMessage());
            log(CoreLoggers.SERVER, Level.INFO, null, null, LogType.END);
        }

        try (PopResource pop = pushContext()) {
            try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw);) {
                exception.printStackTrace(pw);
                String trace = sw.toString();
                putContext("tx.response.trace", trace);
            } catch (IOException cant_happen) {
                putContext("tx.response.trace", "Unable to generate");
            }
            literalField("tx.response.trace").log(CoreLoggers.SERVER, Level.DEBUG, null, null, LogType.END);
        }
    }

    @Override
    public void logIncomingCallEnd(int status, String body) {
        boolean largeBody = false;

        LoggingConfiguration config = getLoggingConfiguration();

        if (status == HttpStatus.OK.value()) {
            putContext("tx.result", 0);
        } else {
            putContext("tx.result", 1);
        }
        putContext("tx.response.status", status);
        putContext("tx.duration", getContextDuration());

        try (PopResource pop = pushContext()) {
            if (body != null) {
                String s = body;
                if (s.length() > config.getMaxInfoResponseBodySize()) {
                    s = s.substring(0, config.getMaxInfoResponseBodySize()) + "[...]";
                    largeBody = true;
                }
                putContext("tx.response.body", s);
            }
            log(CoreLoggers.SERVER, Level.INFO, null, null, LogType.END);
        }

        if (largeBody) {
            try (PopResource pop = pushContext()) {
                String s = body;
                if (s.length() > config.getMaxDebugResponseBodySize()) {
                    s = s.substring(0, config.getMaxDebugResponseBodySize()) + "[...]";
                }
                putContext("tx.response.body", s);
                literalField("tx.response.body").log(CoreLoggers.SERVER, Level.DEBUG, null, null, LogType.END);
            }
        }
    }

    @Override
    public void logOutgoingCallStart(String type, String id, String method, String uri, List<Header> headers, String body, JSONObject params) {
        boolean largeBody = false;
        boolean hasHeaders = headers != null && headers.size() != 0;

        LoggingConfiguration config = null;

        CallLoggerName loggerName = new CallLoggerName(type);
        try (PopResource pop = pushContext()) {
            putContext("call.type", type);
            putContext("call.id", id);
            putContext("call.method", method);
            putContext("call.uri", uri);
            if (params != null && params.length() != 0) {
                putContext("call.request.params", params);
            }

            config = getLoggingConfiguration();
            if (body != null) {
                String s = body;
                if (s.length() > config.getMaxOutInfoRequestBodySize()) {
                    s = s.substring(0, config.getMaxOutInfoRequestBodySize()) + "[...]";
                    largeBody = true;
                }
                putContext("call.request.body", s);
            }

            log(loggerName, Level.INFO, null, null, LogType.START_CALL);
        }

        if (largeBody || hasHeaders) {
            try (PopResource pop = pushContext()) {
                putContext("call.type", type);
                putContext("call.id", id);
                if (hasHeaders) {
                    JSONArray a = new JSONArray();
                    for (Header header : headers) {
                        a.put(header.toString());
                    }
                    putContext("call.request.headers", a);
                }
                if (body != null) {
                    String s = body;
                    if (s.length() > config.getMaxOutDebugRequestBodySize()) {
                        s = s.substring(0, config.getMaxOutDebugRequestBodySize()) + "[...]";
                    }
                    putContext("call.request.body", s);
                }
                literalField("call.request.body").log(loggerName, Level.DEBUG, null, null, LogType.START_CALL);
            }
        }
    }

    @Override
    public void logOutgoingCallEnd(String type, String id, Response<?> response, String body) {
        boolean largeBody = false;
        boolean hasHeaders = response.getHeaders() != null && response.getHeaders().size() != 0;

        boolean callSucceeded = (response.getStatusCode() == 200);
        boolean hasThrowable = (response.getThrowable() != null);

        if (body == null) {
            body = response.getRawPayload();
        }

        Level secondEventLevel = Level.DEBUG;
        if (!callSucceeded || hasThrowable) {
            secondEventLevel = Level.INFO;
        }

        LoggingConfiguration config = null;

        CallLoggerName loggerName = new CallLoggerName(type);
        try (PopResource pop = pushContext()) {
            putContext("call.type", type);
            putContext("call.id", id);
            putContext("call.result", response.getStatus().toString());
            if (response.getStatusCode() != 0) {
                putContext("call.response.status", response.getStatusCode());
            }
            putContext("call.response.status.line", response.getStatusLine());

            addRequestStatisticsToLoggingContext(response);

            config = getLoggingConfiguration();
            if (body != null) {
                String s = body;
                if (s.length() > config.getMaxOutInfoResponseBodySize()) {
                    s = s.substring(0, config.getMaxOutInfoResponseBodySize()) + "[...]";
                    largeBody = true;
                }
                putContext("call.response.body", s);
            }

            log(loggerName, Level.INFO, null, null, LogType.END_CALL);
        }

        if (largeBody || hasHeaders) {
            try (PopResource pop = pushContext()) {
                putContext("call.type", type);
                putContext("call.id", id);

                addHeadersToLoggingContext(response);

                if (body != null) {
                    String s = body;
                    if (s.length() > config.getMaxOutDebugResponseBodySize()) {
                        s = s.substring(0, config.getMaxOutDebugResponseBodySize()) + "[...]";
                    }
                    putContext("call.response.body", s);
                }
                literalField("call.response.body").log(loggerName, secondEventLevel, null, null, LogType.END_CALL);
            }
        }

        if (hasThrowable) {
            try (PopResource pop = pushContext()) {
                Throwable t = response.getThrowable();
                putContext("call.type", type);
                putContext("call.id", id);
                putContext("call.exception.class", t.getClass().getName());
                putContext("call.exception.message", t.getMessage());
                try (StringWriter sw = new StringWriter(); PrintWriter pw = new PrintWriter(sw);) {
                    t.printStackTrace(pw);
                    String trace = sw.toString();
                    putContext("call.exception.trace", trace);
                } catch (IOException cant_happen) {
                    putContext("call.exception.trace", "Unable to generate");
                }
                literalField("call.exception.trace").log(loggerName, secondEventLevel, null, null, LogType.END_CALL);
            }
        }
    }

    private void addHeadersToLoggingContext(Response<?> response) {
        boolean hasHeaders = response.getHeaders() != null && response.getHeaders().size() != 0;
        if (hasHeaders) {
            JSONArray a = new JSONArray();
            for (Header header : response.getHeaders()) {
                a.put(header.toString());
            }
            putContext("call.response.headers", a);
        }
    }

    private void addRequestStatisticsToLoggingContext(Response<?> response) {
        RequestStatistic reqeustStatistic = response.getStatistic();
        double d;

        d = reqeustStatistic.getTotalDuration();
        if (d != 0) {
            putContext("call.duration", d);
        }

        d = reqeustStatistic.getCallbackDuration();
        if (d != 0) {
            putContext("call.duration.callback", d);
        }

        d = reqeustStatistic.getCallTime();
        if (d != 0) {
            putContext("call.duration.backend", d);
        }

        d = reqeustStatistic.getConversionDuration();
        if (d != 0) {
            putContext("call.duration.conversion", d);
        }

        d = reqeustStatistic.getQueueTime();
        if (d != 0) {
            putContext("call.duration.queue", d);
        }

        QueueStatistic queueStatistic = reqeustStatistic.getQueueStatistic();
        if (queueStatistic != null) {
            putContext("queue.name", queueStatistic.getQueueName());

            putContext("queue.tasks.rejected", queueStatistic.getRejectedTaskCount());
            putContext("queue.tasks.completed", queueStatistic.getCompletedTaskCount());
            putContext("queue.tasks.active", queueStatistic.getActiveTaskCount());
            putContext("queue.tasks.pending", queueStatistic.getQueueSize());

            putContext("queue.pool.size", queueStatistic.getPoolSize());
            putContext("queue.pool.core_size", queueStatistic.getCorePoolSize());
            putContext("queue.pool.maximum_size", queueStatistic.getMaximumPoolSize());
            putContext("queue.pool.largest_size", queueStatistic.getLargestPoolSize());
        }
    }

    // Methods for managing the logging context. These just delegate to the
    // LoggingContext instance for the current thread.

    @Override
    public PopResource pushContext() {
        return LoggingContext.get().push();
    }

    @Override
    public PopResource pushContext(LoggingContextMap newContext) {
        return LoggingContext.get().push(newContext);
    }

    @Override
    public void popContext() {
        LoggingContext.get().pop();
    }

    @Override
    public Object removeContext(String key) {
        return LoggingContext.get().remove(key);
    }

    @Override
    public boolean containsContextKey(String key) {
        return LoggingContext.get().containsKey(key);
    }

    @Override
    public boolean isContextEmpty() {
        return LoggingContext.get().isEmpty();
    }

    @Override
    public Object putContext(String key, Object value) {
        return LoggingContext.get().put(key, value);
    }

    @Override
    public Object getContext(String key) {
        return LoggingContext.get().get(key);
    }

    @Override
    public double getContextDuration() {
        return LoggingContext.get().getContextDuration();
    }

    @Override
    public LoggingContextMap getContextSnapshot() {
        return LoggingContext.get().getSnapshot();
    }

    // A few support methods to deal with request/response bodies

    @Override
    public boolean isComplex(Object o) {
        return LoggingContext.getObjectComplexity(o) > getLoggingConfiguration().getComplexityThreshold();
    }

    private boolean looksLikeJson(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isWhitespace(c) || c == 0xFEFF) {
                continue;
            }
            return c == '{';
        }
        return false;
    }

    @Override
    public String prettyPrintJson(String s) {
        try {
            if (s == null) {
                return null;
            }
            if (!looksLikeJson(s)) {
                return s;
            }
            JSONObject o = new JSONObject(s);
            if (isComplex(o)) {
                return o.toString(2);
            }
            return s;
        } catch (Throwable t) {
            return s;
        }
    }

    private boolean looksLikeXml(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isWhitespace(c) || c == 0xFEFF) {
                continue;
            }
            return c == '<';
        }
        return false;
    }

    @Override
    public String prettyPrintXml(String s) {
        try {
            if (s == null) {
                return null;
            }
            if (!looksLikeXml(s)) {
                return s;
            }
            Source xmlInput = new StreamSource(new StringReader(s));
            StringWriter stringWriter = new StringWriter();
            StreamResult xmlOutput = new StreamResult(stringWriter);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            transformerFactory.setAttribute("indent-number", 2);
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.transform(xmlInput, xmlOutput);
            return xmlOutput.getWriter().toString();
        } catch (Throwable t) {
            return s;
        }
    }

    private LoggingConfiguration getLoggingConfiguration() {
        String txType = (String) getContext("tx.type");
        String callType = (String) getContext("call.type");

        LoggingConfiguration configuration = null;
        if (!StringUtils.isEmpty(callType)) {
            configuration = config.getLoggingConfiguration(callType);
        }
        if (configuration == null) {
            configuration = config.getLoggingConfiguration(txType);
        }
        if (configuration == null) {
            configuration = config.getLoggingConfiguration(LogManagerConfiguration.DEFAULT);
        }
        return configuration;
    }

}
