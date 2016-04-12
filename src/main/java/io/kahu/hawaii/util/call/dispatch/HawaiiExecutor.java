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

import io.kahu.hawaii.util.call.AbortableRequest;
import io.kahu.hawaii.util.call.Response;
import io.kahu.hawaii.util.call.statistics.QueueStatistic;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.FutureTask;

public interface HawaiiExecutor {
    String getName();

    void rejectTask();

    QueueStatistic getQueueStatistic();

    <T> FutureTask<T> execute(AbortableRequest<T> request, Response<T> response);

    <T> FutureTask<T> executeAsync(AbortableRequest<T> request, RequestDispatcher dispatcher);

    void shutdown();
}
