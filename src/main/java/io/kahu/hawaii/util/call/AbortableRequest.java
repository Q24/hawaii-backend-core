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

import io.kahu.hawaii.util.call.statistics.QueueStatistic;
import io.kahu.hawaii.util.call.statistics.RequestStatistic;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;

/**
 * Internal interface, should never be used directly. Use Request. For implementations, create a subclass of AbstractAbortableRequest.
 * @param <T>
 */
public interface AbortableRequest<T> extends Request<T> {
    TimeOut getTimeOut();

    RequestContext<T> getContext();

    void setQueueStatistic(QueueStatistic queueStatistic);

    RequestStatistic getStatistic();

    void setCallback(ResponseCallback<T> callback);

    Response<T> doExecute() throws Throwable;

    void doCallback();

    /**
     * In case the request takes too long, the request is aborted by invoking this method.
     */
    void abort();

    /**
     * Signal the end of the request, releasing the lock on the response. Clients using Response#get() will be signalled to conintue.
     */
    void finish();

    /**
     * In case the system is too busy, the request can be rejected.
     */
    void reject();

    /**
     * internal use only
     * @return
     */
    Response<T> getResponse();

    void setLatch(CountDownLatch latch);

    boolean isAsync();
}
