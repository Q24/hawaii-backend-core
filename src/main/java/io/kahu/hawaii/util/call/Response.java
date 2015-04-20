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
package io.kahu.hawaii.util.call;

import io.kahu.hawaii.util.call.statistics.RequestStatistic;
import io.kahu.hawaii.util.exception.ServerError;
import io.kahu.hawaii.util.exception.ServerException;
import io.kahu.hawaii.util.logger.LoggingContextMap;

import java.net.SocketException;
import java.util.List;

import org.apache.http.Header;

public class Response<T> {
    private final Request<T> request;
    private final RequestStatistic statistic;
    private final LoggingContextMap loggingContext;

    private String rawPayload;

    private Throwable throwable;
    private ResponseStatus status;
    private String message;
    private T response;

    // These 3 are mostly for responses to HTTP requests, but can hold metadata
    // from other request types as well.
    // Their values are not required to be as expected in an HTTP response.
    // (e.g. Content-Type, Content-Length, ...)
    // It's safe to leave this uninitialized if they're not relevant for this
    // response.
    private String statusLine = null;
    private int statusCode = 0;
    private List<Header> headers = null;

    private boolean logged = false;

    public Response(Request<T> request, RequestStatistic statistic, LoggingContextMap loggingContext) {
        this.request = request;
        this.statistic = statistic;
        this.loggingContext = loggingContext;
    }

    public T get() throws ServerException {
        if (isOk()) {
            return response;
        }

        if (throwable == null) {
            throw new ServerException(ServerError.UNEXPECTED_EXCEPTION, message);
        } else if (throwable instanceof SocketException) {
            throw new ServerException(ServerError.BACKEND_CONNECTION_ERROR, throwable);
        } else if (throwable instanceof ServerException) {
            throw (ServerException) throwable;
        } else {
            throw new ServerException(ServerError.UNEXPECTED_EXCEPTION, message, throwable);
        }
    }

    public String getRawPayload() {
        return rawPayload;
    }

    public void setRawPayload(String rawPayload) {
        this.rawPayload = rawPayload;
    }

    public void set(T response) {
        setStatus(ResponseStatus.SUCCESS);
        this.response = response;
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public void setStatus(ResponseStatus status) {
        statistic.setStatus(status);
        this.status = status;
    }

    public void setStatus(ResponseStatus status, String message) {
        setStatus(status);
        setMessage(message);
    }

    public void setStatus(ResponseStatus status, T response) {
        setStatus(status);
        this.response = response;
    }

    public void setStatus(ResponseStatus status, String message, Throwable throwable) {
        setStatus(status);
        setMessage(message);
        this.throwable = throwable;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private boolean isOk() {
        return ResponseStatus.SUCCESS.equals(getStatus());
    }

    public Request<T> getRequest() {
        return request;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public synchronized boolean getAndSetLogged(boolean logged) {
        boolean value = this.logged;
        this.logged = logged;
        return value;
    }

    public LoggingContextMap getLoggingContext() {
        return loggingContext;
    }

    public String getStatusLine() {
        return statusLine;
    }

    public void setStatusLine(String statusLine) {
        this.statusLine = statusLine;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public List<Header> getHeaders() {
        return headers;
    }

    public void setHeaders(List<Header> headers) {
        this.headers = headers;
    }

    public T getResponsePayload() {
        return response;
    }

    public RequestStatistic getStatistic() {
        return statistic;
    }
}
