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
package io.kahu.hawaii.util.call.http;

import io.kahu.hawaii.util.call.AbstractAbortableRequest;
import io.kahu.hawaii.util.call.RequestContext;
import io.kahu.hawaii.util.call.Response;
import io.kahu.hawaii.util.call.ResponseHandler;
import io.kahu.hawaii.util.call.ResponseStatus;
import io.kahu.hawaii.util.call.dispatch.RequestDispatcher;
import io.kahu.hawaii.util.call.log.CallLogger;
import io.kahu.hawaii.util.exception.ServerError;
import io.kahu.hawaii.util.exception.ServerException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;

public class AbortableHttpRequest<T> extends AbstractAbortableRequest<HttpResponse, T> implements HttpCall {
    private boolean aborted = false;
    private HttpClientBuilder httpClientBuilder;
    private final HttpRequestBase httpRequest;
    private final HttpClientContext httpClientContext;

    public AbortableHttpRequest(RequestDispatcher requestDispatcher, RequestContext<T> context, ResponseHandler<HttpResponse, T> responseHandler,
            HttpRequestBase httpRequest, CallLogger<T> logger) {
        super(requestDispatcher, context, responseHandler, logger);
        this.httpRequest = httpRequest;
        this.httpClientContext = HttpClientContext.create();
    }

    @Override
    public void setHttpClientBuilder(HttpClientBuilder httpClientBuilder) {
        this.httpClientBuilder = httpClientBuilder;
    }

    @Override
    public HttpRequest getHttpRequest() {
        return httpRequest;
    }

    @Override
    public HttpClientContext getHttpClientContext() {
        return httpClientContext;
    }

    @Override
    protected void executeInternally(ResponseHandler<HttpResponse, T> responseHandler, Response<T> response) throws ServerException {
        try {
            addHawaiiHeaders();
            HttpResponse httpResponse = getHttpClient().execute(httpRequest, httpClientContext);
            responseHandler.addToResponse(httpResponse, response);
        } catch (ClientProtocolException e) {
            throw new ServerException(ServerError.IO, e);
        } catch (IOException e) {
            if (!aborted) {
                response.setStatus(ResponseStatus.BACKEND_FAILURE, e.getMessage(), e);
            }
        } finally {
            httpRequest.releaseConnection();
        }
    }

    private CloseableHttpClient getHttpClient() {
        CloseableHttpClient client = httpClientBuilder.build();
        return client;
    }

    private void addHawaiiHeaders() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        Header header = new BasicHeader("X-Hawaii-Timestamp", sdf.format(new Date()));
        httpRequest.setHeader(header);

        header = new BasicHeader("X-Hawaii-Id", getId());
        httpRequest.setHeader(header);
    }

    @Override
    protected void abortInternally() {
        httpRequest.abort();
        aborted = true;
    }

}
