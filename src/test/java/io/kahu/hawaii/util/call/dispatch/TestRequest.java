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
package io.kahu.hawaii.util.call.dispatch;

import io.kahu.hawaii.util.call.AbstractAbortableRequest;
import io.kahu.hawaii.util.call.RequestContext;
import io.kahu.hawaii.util.call.Response;
import io.kahu.hawaii.util.call.ResponseHandler;
import io.kahu.hawaii.util.call.log.CallLogger;
import io.kahu.hawaii.util.call.statistics.RequestStatistic;
import io.kahu.hawaii.util.exception.ServerError;
import io.kahu.hawaii.util.exception.ServerException;

import java.util.concurrent.CountDownLatch;

public class TestRequest extends AbstractAbortableRequest<String, String> {
    private CountDownLatch latch = new CountDownLatch(1);

    private boolean aborted = false;
    private boolean rejected = false;

    public TestRequest(RequestDispatcher requestDispatcher, RequestContext<String> context, ResponseHandler<String, String> responseHandler, CallLogger<String> logger) {
        super(requestDispatcher, context, responseHandler, logger);
        setStatistic(new RequestStatistic());
        setResponse(new Response<>(this, this.getStatistic(), null));
    }

    @Override
    protected void executeInternally(ResponseHandler<String, String> responseHandler, Response<String> response) throws ServerException {
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new ServerException(ServerError.UNEXPECTED_EXCEPTION, e);
        }

        responseHandler.addToResponse("Backend response.", response);
    }

    @Override
    protected void abortInternally() {
        aborted = true;
    }

    protected void rejectInternally() {
        rejected = true;
    }


    @Override
    public String getId() {
        return null;
    }

    @Override
    public String getCallName() {
        return null;
    }

    public void proceed() {
        latch.countDown();
    }

    public boolean isAborted() {
        return aborted;
    }

    public boolean isRejected() {
        return rejected;
    }
}
