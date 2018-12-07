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
package io.kahu.hawaii.util.spring;

import io.kahu.hawaii.util.exception.AuthorisationException;
import io.kahu.hawaii.util.exception.HawaiiException;
import io.kahu.hawaii.util.exception.ValidationException;
import io.kahu.hawaii.util.logger.CoreLoggers;
import io.kahu.hawaii.util.logger.LogManager;
import io.kahu.hawaii.util.logger.LoggingContext;

import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class HawaiiControllerExceptionHandler extends ResponseEntityExceptionHandler {
    private static final String X_HAWAII_TRANSACTION_ID_HEADER = "X-Hawaii-Tx-Id";
    private final LogManager logManager;
    private final List<Class<?>> exceptionsToIgnore = new ArrayList<>();

    public HawaiiControllerExceptionHandler(LogManager logManager, Class<?>... exceptionsToIgnore) {
        this.logManager = logManager;
        if (exceptionsToIgnore != null) {
            Collections.addAll(this.exceptionsToIgnore, exceptionsToIgnore);
        }
    }

    @ExceptionHandler(HawaiiException.class)
    @ResponseBody
    public ResponseEntity<String> catchHawaiiException(HawaiiException throwable) {
        JSONObject error = new JSONObject();
        try {
            error = throwable.toJson();
        } catch (HawaiiException exc) {
            logManager.error(CoreLoggers.SERVER_EXCEPTION, exc.getMessage(), exc);
        }

        return handleException(throwable, throwable.getStatus().value(), error);
    }

    @ExceptionHandler(UndeclaredThrowableException.class)
    @ResponseBody
    public ResponseEntity<String> catchUndeclaredThrowable(Throwable throwable) {
        Throwable cause = throwable.getCause();
        if (cause instanceof HawaiiException) {
            return catchHawaiiException((HawaiiException) cause);
        }
        return handleException(throwable, 500, new JSONObject());
    }

    @ExceptionHandler(Throwable.class)
    @ResponseBody
    public ResponseEntity<String> catchAll(Throwable throwable) {
        return handleException(throwable, 500, new JSONObject());
    }

    private void log(Throwable throwable) {
        if (mustLog(throwable)) {
            logManager.warn(CoreLoggers.SERVER_EXCEPTION, throwable.getMessage(), throwable);
        } else {
            if (throwable instanceof AuthorisationException) {
                logManager.debug(CoreLoggers.SERVER, throwable.getMessage());
            }
            logManager.trace(CoreLoggers.SERVER_EXCEPTION, throwable.getMessage(), throwable);
        }
    }

    private ResponseEntity<String> handleException(Throwable throwable, int httpStatusCode, JSONObject error) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Object hawaiiTxId = LoggingContext.get().get("tx.id");
        if (hawaiiTxId != null) {
            headers.set(X_HAWAII_TRANSACTION_ID_HEADER, hawaiiTxId.toString());
        }
        JSONObject json = new JSONObject();
        try {
            json.put("status", httpStatusCode);
            json.put("data", new JSONArray());
            json.put("error", error);
        } catch (JSONException exc) {
            logManager.error(CoreLoggers.SERVER_EXCEPTION, exc.getMessage(), exc);
        }

        log(throwable);

        return ResponseEntity.ok().headers(headers).body(json.toString());
    }

    @SuppressWarnings("SimplifiableIfStatement")
    private boolean mustLog(Throwable throwable) {
        if (throwable instanceof ValidationException) {
            return ((ValidationException)throwable).containsRequestValidationError();
        }
        if (throwable instanceof AuthorisationException) {
            return false;
        }
        return !exceptionsToIgnore.contains(throwable.getClass());
    }
}
