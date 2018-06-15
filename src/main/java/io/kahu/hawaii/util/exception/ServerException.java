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
package io.kahu.hawaii.util.exception;

import io.kahu.hawaii.util.logger.CoreLoggers;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;

public class ServerException extends HawaiiException {

    private static final long serialVersionUID = 8024960096864705454L;
    private HawaiiServerError error;

    public ServerException(HawaiiServerError error, String message, Throwable throwable) {
        /**
         * To avoid NPE when calling the super constructor which needs to be the
         * first call in this constructor
         */
        super((error != null ? error.toString() : "") + (message != null ? " - " + message : ""), throwable);
        Assert.notNull(error);
        this.error = error;
    }

    public HawaiiServerError getError() {
        return error;
    }

    public void setError(HawaiiServerError error) {
        this.error = error;
    }

    public ServerException(HawaiiServerError error, Throwable throwable) {
        this(error, null, throwable);
    }

    public ServerException(HawaiiServerError error, String message) {
        this(error, message, null);
    }

    public ServerException(HawaiiServerError error) {
        this(error, null, null);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.INTERNAL_SERVER_ERROR; // 500
    }

    @Override
    public CoreLoggers getLoggerName() {
        return CoreLoggers.SERVER;
    }

    @Override
    public JSONObject toJson() throws HawaiiException {
        if (ServerError.BACKEND_CONNECTION_ERROR.equals(error) || ServerError.DATABASE_CONNECTION_ERROR.equals(error)) {
            try {
                JSONObject result = new JSONObject();
                result.put("SERVER_ERROR", error);
                return result;
            } catch (JSONException e) {
                throw new ServerException(ServerError.JSON, e);
            }

        }
        return super.toJson();
    }
}
