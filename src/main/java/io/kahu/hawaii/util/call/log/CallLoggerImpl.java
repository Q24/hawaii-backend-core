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
package io.kahu.hawaii.util.call.log;

import io.kahu.hawaii.util.call.Request;
import io.kahu.hawaii.util.call.Response;
import io.kahu.hawaii.util.call.log.request.RequestLogger;
import io.kahu.hawaii.util.call.log.response.ResponseLogger;
import io.kahu.hawaii.util.logger.LogManager;
import io.kahu.hawaii.util.logger.LoggingContext.PopResource;
import org.apache.http.annotation.ThreadSafe;

@ThreadSafe
public class CallLoggerImpl<T> implements CallLogger<T> {
    protected LogManager logManager;
    protected RequestLogger requestLogger;
    protected ResponseLogger<T> responseLogger;

    public CallLoggerImpl(LogManager logManager, RequestLogger requestLogger, ResponseLogger<T> responseLogger) {
        assert logManager != null;
        this.logManager = logManager;
        this.requestLogger = requestLogger;
        this.responseLogger = responseLogger;
    }

    @Override
    public LogManager getLogManager() {
        return logManager;
    }

    @Override
    public void logRequest(Request<T> request) {
        if (requestLogger != null) {
            requestLogger.logRequest(logManager, request);
        }
    }

    @Override
    public void logResponse(Response<T> response) {
        if (!response.getAndSetLogged(true)) {
            try (PopResource pop = logManager.pushContext(response.getLoggingContext())) {
                if (responseLogger != null) {
                    responseLogger.logResponse(logManager, response);
                }
            }
        }
    }
}
