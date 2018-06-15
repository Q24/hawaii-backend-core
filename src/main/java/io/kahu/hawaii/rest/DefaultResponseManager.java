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
package io.kahu.hawaii.rest;

import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;

import io.kahu.hawaii.util.exception.*;
import io.kahu.hawaii.util.logger.CoreLoggers;
import io.kahu.hawaii.util.logger.LogManager;
import io.kahu.hawaii.util.logger.LoggingContext;

public class DefaultResponseManager implements ResponseManager {

    protected static final String X_HAWAII_TRANSACTION_ID_HEADER = "X-Hawaii-Tx-Id";

    private final LogManager logManager;

    private final String loggingContentTxId;
    private final boolean hawaiiTxIdHeaderEnabled;

    public DefaultResponseManager(LogManager logManager, String loggingContextTxId) {
        Assert.notNull(logManager);
        this.logManager = logManager;
        this.loggingContentTxId = loggingContextTxId;
        this.hawaiiTxIdHeaderEnabled = StringUtils.isNotBlank(loggingContextTxId);
    }

    @Override
    public ResponseEntity<String> toResponse(Throwable throwable) {
        /**
         * This method is activated as a final step method-execution
         * (exception-handling) before control is given back to the container
         * and can therefore not assert that the supplied exception is not null
         */
        if (throwable == null) {
            return toResponse(new ServerException(ServerError.ILLEGAL_ARGUMENT));
        }
        if (!(throwable instanceof HawaiiException)) {
            return toResponse(new ServerException(ServerError.UNEXPECTED_EXCEPTION, throwable));
        }
        if (mustLog(throwable)) {
            logManager.debug(CoreLoggers.SERVER_EXCEPTION, throwable.getMessage(), throwable);
        }
        HawaiiException exception = (HawaiiException) throwable;

        /**
         * The alternative was to have the HawaiiException try-catch the
         * json-exceptions (and possible other runtime exceptions) and log the
         * error. That created the side-effect that the LogManager (no longer
         * static) needed to be inserted into every newly created instance of
         * the HawaiiException. There are ways to do that with Spring (at this
         * moment not known to me) but this solution feels better after all.
         */
        JSONObject response = new JSONObject();
        try {
            response.put(STATUS_KEY, exception.getStatus().value());
            response.put(DATA_KEY, new JSONArray());
            response.put(ERROR_KEY, exception.toJson());
        } catch (Throwable very_unlikely) {
            logManager.error(new ServerException(ServerError.UNEXPECTED_EXCEPTION, very_unlikely));
        }
        if (throwable instanceof ValidationException) {
            try {
                logManager.logIncomingCallEnd(HttpStatus.BAD_REQUEST.value(), response.toString(2));
            } catch (Throwable very_unlikely) {
                logManager.error(new ServerException(ServerError.UNEXPECTED_EXCEPTION, very_unlikely));
            }
        } else if (throwable instanceof AuthorisationException) {
            logManager.logIncomingCallEnd(HttpStatus.OK.value(), throwable.getMessage());
        } else {
            logManager.logIncomingCallEnd(exception);
        }

        return myToResponse(response);
    }

    private boolean mustLog(Throwable throwable) {
        if (throwable instanceof ValidationException) {
            return false;
        }
        if (throwable instanceof AuthorisationException) {
            return false;
        }
        return true;
    }

    @Override
    public ResponseEntity<String> toResponse(JSONObject... jsonObjects) throws ServerException {
        Assert.notNull(jsonObjects);
        Assert.isTrue(jsonObjects.length > 0);

        JSONArray data = new JSONArray();

        for (JSONObject jsonObject : jsonObjects) {
            assert jsonObject != null;
            data.put(jsonObject);
        }

        return myToResponse(data);
    }

    @Override
    public ResponseEntity<String> toResponse(JSONArray jsonObjects) throws ServerException {
        Assert.notNull(jsonObjects);
        return myToResponse(jsonObjects);
    }

    @Override
    public ResponseEntity<String> toResponse(JSONSerializable... objects) throws ServerException {
        Assert.notNull(objects);
        Assert.isTrue(objects.length > 0);

        JSONArray data = new JSONArray();
        for (JSONSerializable object : objects) {
            assert object != null;
            data.put(object.toJson());
        }
        return myToResponse(data);
    }

    @Override
    public ResponseEntity<String> toResponse() throws ServerException {
        return myToResponse(new JSONArray());
    }

    @Override
    public ResponseEntity<String> toResponse(List<JSONSerializable> objects) throws ServerException {
        Assert.notNull(objects);
        JSONArray data = new JSONArray();
        for (JSONSerializable object : objects) {
            data.put(object.toJson());
        }
        return myToResponse(data);
    }

    private ResponseEntity<String> myToResponse(JSONArray data) throws ServerException {
        try {
            JSONObject response = new JSONObject();
            response.put(STATUS_KEY, HttpStatus.OK.value());
            response.put(DATA_KEY, data);
            response.put(ERROR_KEY, new JSONObject());
            logManager.logIncomingCallEnd(HttpStatus.OK.value(), response.toString(2));
            return myToResponse(response);
        } catch (JSONException e) {
            throw new ServerException(ServerError.JSON, e);
        }
    }

    private ResponseEntity<String> myToResponse(JSONObject response) {
        HttpHeaders headers = new HttpHeaders();
        // explicitly set the application/json content type
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (hawaiiTxIdHeaderEnabled) {
            // note the X-Hawaii-Tx-Id header can be disabled by setting
            // logging.context.txid=
            LoggingContext loggingContext = LoggingContext.get();
            Object hawaiiTxId = loggingContext.get(this.loggingContentTxId);
            if (hawaiiTxId != null) {
                headers.set(X_HAWAII_TRANSACTION_ID_HEADER, ObjectUtils.toString(hawaiiTxId));
            }
        }
        String json = response.toString();
        return ResponseEntity.ok().headers(headers).body(json);
    }
}
