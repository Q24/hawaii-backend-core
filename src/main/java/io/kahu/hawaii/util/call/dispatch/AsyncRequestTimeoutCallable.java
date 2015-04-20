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

import io.kahu.hawaii.util.call.RequestContext;
import io.kahu.hawaii.util.call.Response;
import io.kahu.hawaii.util.logger.CoreLoggers;
import io.kahu.hawaii.util.logger.LogManager;
import io.kahu.hawaii.util.logger.LoggingContext;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public class AsyncRequestTimeoutCallable<T> implements Callable<Response<T>> {
    private final LogManager logManager;
    private final FutureRequest<T> asyncTask;

    public AsyncRequestTimeoutCallable(FutureRequest<T> asyncTask, LogManager logManager) {
        this.asyncTask = asyncTask;
        this.logManager = logManager;
    }

    /**
     * {@inheritDoc}
     *
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Override
    public Response<T> call() throws InterruptedException, ExecutionException {
        LoggingContext.remove();
        RequestContext<T> context = asyncTask.getAbortableRequest().getContext();
        int timeOut = context.getTimeOut();
        try {
            if (timeOut < 0) {
                timeOut = 60;
            }

            if (!asyncTask.isDone()) {
                // block and wait
                return asyncTask.get(timeOut, context.getTimeOutUnit());
            }
        } catch (TimeoutException e) {
            logManager.info(CoreLoggers.SERVER, "Terminating async call '" + context + "' with id '" + asyncTask.getAbortableRequest().getId() + "' due to timeout of '"
                    + timeOut + "' seconds.");
            asyncTask.getAbortableRequest().abort();
        }

        return null;
    }

}
