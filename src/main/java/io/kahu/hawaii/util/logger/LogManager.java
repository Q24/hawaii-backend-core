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
import io.kahu.hawaii.util.exception.HawaiiException;
import io.kahu.hawaii.util.logger.LoggingContext.PopResource;

import java.util.List;

import org.apache.http.Header;
import org.codehaus.jettison.json.JSONObject;

public interface LogManager {

    void error(HawaiiException e);

    void audit(AuditTrail trail);

    void audit(String message);

    void setLevel(LoggerName name,
                    String level);

    void trace(LoggerName name,
                    String message);

    void trace(LoggerName name,
                    String message,
                    Throwable t);

    void debug(LoggerName name,
                    String message);

    void debug(LoggerName name,
                    String message,
                    Throwable t);

    void info(LoggerName name,
                    String message);

    void info(LoggerName name,
                    String message,
                    Throwable t);

    void warn(LoggerName name,
                    String message);

    void warn(LoggerName name,
                    String message,
                    Throwable t);

    void error(LoggerName name,
                    String message);

    void error(LoggerName name,
                    Throwable t);

    void error(LoggerName name,
                    String message,
                    Throwable t);

    void fatal(LoggerName name,
                    String message);

    void fatal(LoggerName name,
                    String message,
                    Throwable t);

    void logIncomingCallStart(String type,
                    String body,
                    JSONObject params);

    void logIncomingCallEnd(Throwable t);

    void logIncomingCallEnd(int status,
                    String body);

    void logOutgoingCallStart(String type,
                    String id,
                    String method,
                    String uri,
                    List<Header> headers,
                    String body,
                    JSONObject params);

    void logOutgoingCallEnd(String type,
                    String id,
                    Response<?> response,
                    String body);

    LogManager literalField(String field);

    // Methods for managing the logging context

    PopResource pushContext();

    PopResource pushContext(LoggingContextMap newContext);

    void popContext();

    Object removeContext(String key);

    boolean containsContextKey(String key);

    boolean isContextEmpty();

    Object putContext(String key,
                    Object value);

    Object getContext(String key);

    double getContextDuration();

    LoggingContextMap getContextSnapshot();

    // A few support methods to deal with request/response bodies

    boolean isComplex(Object o);

    String prettyPrintJson(String s);

    String prettyPrintXml(String s);
}
