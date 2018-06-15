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
package io.kahu.hawaii.util.call.http.response;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.kahu.hawaii.util.call.Response;
import io.kahu.hawaii.util.call.ResponseHandler;
import io.kahu.hawaii.util.call.ResponseStatus;
import io.kahu.hawaii.util.call.http.util.HttpResponseToStringConverter;
import io.kahu.hawaii.util.exception.ServerException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;

public abstract class AbstractHttpResponseHandler<T> implements ResponseHandler<HttpResponse, T> {
    private boolean mayReadPayload = true;
    private final List<Integer> acceptStatusses = new ArrayList<>();

    public AbstractHttpResponseHandler(boolean mayReadPayload) {
        this(mayReadPayload, Collections.singletonList(HttpStatus.SC_OK));

    }

    public AbstractHttpResponseHandler(boolean mayReadPayload, List<Integer> acceptStatusses) {
        this.mayReadPayload = mayReadPayload;
        this.acceptStatusses.addAll(acceptStatusses);
    }

    @Override
    public void addToResponse(HttpResponse payload, Response<T> response) throws ServerException {
        if (mayReadPayload) {
            response.setRawPayload(new HttpResponseToStringConverter().toString(payload));
        }

        StatusLine statusLine = payload.getStatusLine();
        response.setStatusLine(statusLine.toString());
        response.setStatusCode(statusLine.getStatusCode());
        List<Header> l = new ArrayList<>();
        if (payload.getAllHeaders() != null) {
            for (Header header : payload.getAllHeaders()) {
                l.add(header);
            }
        }
        response.setHeaders(l);

        if (accept(statusLine.getStatusCode())) {
            try {
                doAddToResponse(payload, response);
            } catch (Exception | AssertionError exception) {
                response.setStatus(ResponseStatus.BACKEND_FAILURE, exception);
            } catch (Throwable t) {
                response.setStatus(ResponseStatus.INTERNAL_FAILURE, t);
            }
        } else {
            response.setStatus(ResponseStatus.BACKEND_FAILURE, statusLine.getStatusCode() + " " + statusLine.getReasonPhrase());
        }
    }

    protected abstract void doAddToResponse(HttpResponse payload, Response<T> response) throws Exception;

    protected boolean accept(int statusCode) {
        return acceptStatusses.contains(statusCode);
    }
}
