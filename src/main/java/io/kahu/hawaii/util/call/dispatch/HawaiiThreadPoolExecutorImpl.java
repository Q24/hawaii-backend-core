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

import io.kahu.hawaii.util.call.statistics.QueueStatistic;
import io.kahu.hawaii.util.call.statistics.QueueStatisticImpl;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class HawaiiThreadPoolExecutorImpl extends ThreadPoolExecutor implements HawaiiThreadPoolExecutor {
    private final AtomicLong rejected = new AtomicLong(0L);
    private final String name;

    public HawaiiThreadPoolExecutorImpl(String name, int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
            BlockingQueue<Runnable> workQueue, ThreadFactory factory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, new HawaiiBlockingQueue<Runnable>(workQueue), factory, new HawaiiRejectedExecutionHandler(
                handler));
        this.name = name;
    }

    public HawaiiThreadPoolExecutorImpl(String name, int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
            BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, new HawaiiBlockingQueue<Runnable>(workQueue), new HawaiiRejectedExecutionHandler(handler));
        this.name = name;
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
}
