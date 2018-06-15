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
package io.kahu.hawaii.util.call.http;

import io.kahu.hawaii.util.call.Request;
import io.kahu.hawaii.util.call.Response;
import io.kahu.hawaii.util.call.ResponseStatus;
import io.kahu.hawaii.util.call.statistics.RequestStatistic;
import io.kahu.hawaii.util.exception.ServerError;
import io.kahu.hawaii.util.exception.ServerException;
import io.kahu.hawaii.util.logger.LoggingContextMap;
import org.apache.http.Header;

import java.net.SocketException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class HttpRequestResponse<T> extends Response<T> {

    // These 3 are mostly for responses to HTTP requests, but can hold metadata
    // from other request types as well.
    // Their values are not required to be as expected in an HTTP response.
    // (e.g. Content-Type, Content-Length, ...)
    // It's safe to leave this uninitialized if they're not relevant for this
    // response.
    private String statusLine = null;
    private int statusCode = 0;
    private List<Header> headers = null;

    public HttpRequestResponse(Request<T> request, RequestStatistic statistic, LoggingContextMap loggingContext) {
        super(request, statistic, loggingContext);
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

}
