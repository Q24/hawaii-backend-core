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

import io.kahu.hawaii.util.call.dispatch.RequestDispatcher;
import io.kahu.hawaii.util.call.log.CallLogger;
import io.kahu.hawaii.util.call.statistics.RequestStatistic;
import io.kahu.hawaii.util.exception.ServerException;
import io.kahu.hawaii.util.logger.CoreLoggers;
import io.kahu.hawaii.util.logger.LoggingContext.PopResource;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;

public abstract class AbstractAbortableRequest<F, T> implements Request<T>, AbortableRequest<T> {
    private final RequestDispatcher requestDispatcher;
    private final ResponseHandler<F, T> responseHandler;
    private final RequestContext<T> context;

    private RequestStatistic statistic;
    private ResponseCallback<T> callback;
    private final CallLogger<T> logger;

    private boolean isAsync = false;
    private boolean error = false;
    private Response<T> response = null;
    private String id;
    private CountDownLatch latch;

    public AbstractAbortableRequest(RequestDispatcher requestDispatcher, RequestContext<T> context, ResponseHandler<F, T> responseHandler, CallLogger<T> logger) {
        this.requestDispatcher = requestDispatcher;
        this.context = context;
        this.responseHandler = responseHandler;
        this.logger = logger;
    }

    @Override
    public void setCallback(ResponseCallback<T> callback) {
        this.callback = callback;
    }

    @Override
    public void abort() {
        this.error = true;
        statistic.endBackendRequest();
        response.setMessage("Request '" + getId() + "' timed out.");
        response.setStatus(ResponseStatus.TIME_OUT, getContext().getTimeOutResponse());
        abortInternally();
    }

    @Override
    public Response<T> execute() throws ServerException {
        try (PopResource pop = logger.getLogManager().pushContext()) {
            setup();
            return requestDispatcher.execute(this);
        }
    }

    @Override
    public Response<T> executeAsync() throws ServerException {
        try (PopResource pop = logger.getLogManager().pushContext()) {
            setup();
            this.isAsync = true;
            return requestDispatcher.executeAsync(this);
        }
    }

    private void setup() {
        Object idFromContext = logger.getLogManager().getContext("call.id");
        if (idFromContext != null && !idFromContext.equals("")) {
            id = idFromContext.toString();
        } else {
            id = UUID.randomUUID().toString();
        }
        statistic = new RequestStatistic();
        statistic.startRequest();
        logger.logRequest(this);

        response = new Response<>(this, statistic, logger.getLogManager().getContextSnapshot());
        this.isAsync = false;
        this.error = false;
    }

    @Override
    public Response<T> doExecute() throws Throwable {
        try {
            statistic.startBackendRequest();
            executeInternally(new TimingResponseHandler<>(responseHandler, statistic), response);
        } catch (Throwable t) {
            response.setStatus(ResponseStatus.INTERNAL_FAILURE, "Error executing call.", t);
            throw t;
        } finally {
            statistic.endBackendRequest();
        }
        return response;
    }

    protected abstract void executeInternally(ResponseHandler<F, T> responseHandler, Response<T> response) throws ServerException;

    protected abstract void abortInternally();

    protected void rejectInternally() {
        // Do nothing;
    }

    @Override
    public String toString() {
        return getContext().toString();
    }

    @Override
    public RequestContext<T> getContext() {
        return context;
    }

    @Override
    public void doCallback() {
        if (callback != null && !error) {
            try {
                statistic.startCallback();
                callback.handle(response.getResponsePayload());
            } catch (Exception e) {
                logger.getLogManager().error(CoreLoggers.SERVER_CALLS, e);
            } finally {
                statistic.endCallback();
            }
        }

        if (latch != null) {
            latch.countDown();
        }
    }

    @Override
    public RequestStatistic getStatistic() {
        return statistic;
    }

    @Override
    public void finish() {
        statistic.endRequest();
        response.signalDone();
        logResponse();
    }

    @Override
    public void reject() {
        this.error = true;
        response.setMessage("Request '" + getId() + "' rejected, too busy.");
        response.setStatus(ResponseStatus.TOO_BUSY, getContext().getRejectResponse());
        rejectInternally();
    }


    @Override
    public Response<T> getResponse() {
        return response;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getCallName() {
        return context.toString();
    }

    @Override
    public void setLatch(CountDownLatch latch) {
        this.latch = latch;
    }

    public void logResponse() {
        logger.logResponse(response);
    }

    @Override
    public boolean isAsync() {
        return isAsync;
    }

    /*
     * For test purposes
     */
    protected void setStatistic(RequestStatistic statistic) {
        this.statistic = statistic;
    }

    /*
     * For test purposes
     */
    protected void setResponse(Response<T> response) {
        this.response = response;
    }
}
