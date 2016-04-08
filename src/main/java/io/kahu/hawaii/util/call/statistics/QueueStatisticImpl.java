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
package io.kahu.hawaii.util.call.statistics;

import io.kahu.hawaii.util.call.dispatch.HawaiiThreadPoolExecutorImpl;
import org.apache.http.annotation.ThreadSafe;

@ThreadSafe
public class QueueStatisticImpl implements QueueStatistic {
    private final String queueName;
    private final int poolSize;
    private final int corePoolSize;
    private final int maximumPoolSize;
    private final int largestPoolSize;
    private final int queueSize;
    private final int activeTaskCount;
    private final long completedTaskCount;
    private final long rejectedTaskCount;

    public QueueStatisticImpl(HawaiiThreadPoolExecutorImpl executor) {
        this.queueName = executor.getName();

        this.poolSize = executor.getPoolSize();
        this.corePoolSize = executor.getCorePoolSize();
        this.maximumPoolSize = executor.getMaximumPoolSize();
        this.largestPoolSize = executor.getLargestPoolSize();

        this.queueSize = executor.getQueue().size();

        this.activeTaskCount = executor.getActiveCount();
        this.completedTaskCount = executor.getCompletedTaskCount();
        this.rejectedTaskCount = executor.getRejectedTaskCount();
    }

    @Override
    public String getQueueName() {
        return queueName;
    }

    @Override
    public int getPoolSize() {
        return poolSize;
    }

    @Override
    public int getCorePoolSize() {
        return corePoolSize;
    }

    @Override
    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }

    @Override
    public int getLargestPoolSize() {
        return largestPoolSize;
    }

    @Override
    public int getQueueSize() {
        return queueSize;
    }

    @Override
    public int getActiveTaskCount() {
        return activeTaskCount;
    }

    @Override
    public long getCompletedTaskCount() {
        return completedTaskCount;
    }

    @Override
    public long getRejectedTaskCount() {
        return rejectedTaskCount;
    }

    @Override
    public String toString() {
        return "'" + queueName + "' \n\tpool_size '" + poolSize + "' \n\tcorePoolSize '" + corePoolSize + "' \n\tmaxPoolSize '" + maximumPoolSize
                + "' \n\tlargestPoolSize '" + largestPoolSize + "' \n\tqueueSize '" + queueSize + "' \n\tactiveTaskCount '" + activeTaskCount
                + "' \n\tcompletedTaskCount '" + completedTaskCount + "' \n\trejectedTaskCount '" + rejectedTaskCount + "'.";
    }
}
