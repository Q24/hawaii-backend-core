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
import io.kahu.hawaii.util.call.TimeOut;
import io.kahu.hawaii.util.call.statistics.QueueStatistic;
import io.kahu.hawaii.util.call.statistics.QueueStatisticImpl;
import io.kahu.hawaii.util.logger.CoreLoggers;
import io.kahu.hawaii.util.logger.LogManager;
import io.kahu.hawaii.util.logger.LoggingContext;
import org.apache.http.annotation.ThreadSafe;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implementation of an ThreadPoolExecutor that first adds new threads and then queues tasks.
 */
@ThreadSafe
public class HawaiiExecutorImpl extends ThreadPoolExecutor implements HawaiiExecutor {
    private final AtomicLong rejected = new AtomicLong(0L);
    private final String name;
    private final LogManager logManager;

    public HawaiiExecutorImpl(String name, int corePoolSize, int maximumPoolSize, int queueSize, TimeOut threadKeepAlive, LogManager logManager) {
        this(name, corePoolSize, maximumPoolSize, threadKeepAlive, new ArrayBlockingQueue<>(queueSize), new HawaiiThreadFactory(name), null, logManager);
    }

    public HawaiiExecutorImpl(String name, int corePoolSize, int maximumPoolSize, TimeOut threadKeepAlive,
                              BlockingQueue<Runnable> workQueue, ThreadFactory factory, RejectedExecutionHandler handler, LogManager logManager) {
        super(corePoolSize, maximumPoolSize, threadKeepAlive.getDuration(), threadKeepAlive.getUnit(), new HawaiiBlockingQueue<>(workQueue), factory, new HawaiiRejectedExecutionHandler(logManager,
                handler));
        this.name = name;
        this.logManager = logManager;
    }

    public void execute(Runnable command) {
        super.execute(command);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void rejectTask() {
        rejected.incrementAndGet();
    }

    public Long getRejectedTaskCount() {
        return rejected.get();
    }

    @Override
    public QueueStatistic getQueueStatistic() {
        return new QueueStatisticImpl(this);
    }

    public <T> FutureTask<T> execute(AbortableRequest<T> request, Response<T> response) {
        QueueStatistic queueStatistics = getQueueStatistic();
        request.setQueueStatistic(queueStatistics);

        logScheduleStart(request, queueStatistics);

        return doExecute(new FutureRequest(request, response));
    }


    public <T> FutureTask<T> executeAsync(AbortableRequest<T> request, RequestDispatcher dispatcher) {
        QueueStatistic queueStatistics = getQueueStatistic();
        request.setQueueStatistic(queueStatistics);

        logScheduleStart(request, queueStatistics);

        return doExecute(new AsyncFutureRequest(request, dispatcher));
    }


    public <T> FutureTask<T> doExecute(FutureTask<T> task) {
        super.execute(task);
        return task;
    }

    private <T> void logScheduleStart(AbortableRequest<T> request, QueueStatistic queueStatistics) {
        try (LoggingContext.PopResource pushContext = logManager.pushContext()) {
            logManager.putContext("queue.name", getName());

            logManager.putContext("pool.size.current", queueStatistics.getPoolSize());
            logManager.putContext("pool.size.max", queueStatistics.getMaximumPoolSize());
            logManager.putContext("pool.size.largest", queueStatistics.getLargestPoolSize());
            logManager.putContext("pool.task.pending", queueStatistics.getQueueSize());
            logManager.putContext("pool.task.active", queueStatistics.getActiveTaskCount());
            logManager.putContext("pool.task.completed", queueStatistics.getCompletedTaskCount());
            logManager.putContext("pool.task.rejected", queueStatistics.getRejectedTaskCount());

            logManager.info(CoreLoggers.SERVER, "Scheduling " + (request.isAsync() ? "asynchronous " : "") + "request '" + request.getCallName() + "' with id '" + request.getId() + "'.");
        }
    }
}
