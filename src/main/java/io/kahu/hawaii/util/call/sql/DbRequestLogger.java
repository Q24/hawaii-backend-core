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
package io.kahu.hawaii.util.call.sql;

import io.kahu.hawaii.util.call.Request;
import io.kahu.hawaii.util.call.log.request.GenericRequestLogger;
import io.kahu.hawaii.util.call.log.request.RequestLogger;
import io.kahu.hawaii.util.logger.CoreLoggers;
import io.kahu.hawaii.util.logger.LogManager;
import org.apache.http.annotation.ThreadSafe;

import java.util.Map;

@ThreadSafe
public class DbRequestLogger implements RequestLogger {
    @Override
    public void logRequest(LogManager logManager, Request<?> request) {
        if (!(request instanceof DbCall)) {
            logManager.error(CoreLoggers.SERVER_CALLS, "Request for DbQueryLogger is not a DbCall!");
            new GenericRequestLogger().logRequest(logManager, request);
            return;
        }

        DbCall dbCall = (DbCall) request;

        StringBuilder builder = new StringBuilder(dbCall.getSql());
        appendValues(builder, dbCall.getParameters(), true);

        logManager.logOutgoingCallStart(request.getCallName(), request.getId(), null, null, null, builder.toString(), null);
    }


    private void appendValues(final StringBuilder builder, final Map<String, ?> values, final boolean append) {
        if (append) {
            builder.append("\n\tvalues:\n");
        }
        boolean concat = false;
        builder.append("\t{");
        if (values != null) {
            for (Map.Entry<String, ?> entry : values.entrySet()) {
                if (concat) {
                    builder.append(", \n\t");
                }
                builder.append('"');
                builder.append(entry.getKey());
                builder.append("\": \"");
                builder.append(entry.getValue());
                builder.append('"');

                concat = true;
            }
        }
        builder.append('}');
    }
}
