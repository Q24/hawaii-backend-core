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

import io.kahu.hawaii.util.call.*;
import io.kahu.hawaii.util.call.log.CallLogger;
import io.kahu.hawaii.util.call.statistics.QueueStatistic;
import io.kahu.hawaii.util.exception.ServerException;
import io.kahu.hawaii.util.logger.DefaultLogManager;
import io.kahu.hawaii.util.logger.LogManager;
import io.kahu.hawaii.util.logger.LogManagerConfiguration;
import io.kahu.hawaii.util.logger.LoggingConfiguration;
import org.junit.After;
import org.mockito.Mock;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class AbstractDispatcherFrameworkTest {
    private LogManager logManager = new DefaultLogManager(new LogManagerConfiguration(new LoggingConfiguration()));

    private HawaiiExecutorImpl executor;

    @Mock
    private CallLogger<String> callLogger;

    @After
    public void tearDown() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    protected HawaiiExecutorImpl getExecutor() {
        return executor;
    }

    protected LogManager getLogManager() {
        return logManager;
    }

    protected CallLogger<String> getCallLogger() {
        return callLogger;
    }

    protected TestRequest createRequest() {
        return createRequest(0);
    }

    protected TestRequest createRequest(int timeOut) {
        return createRequest(timeOut, null);
    }

    protected TestRequest createRequest(int timeOut, ResponseHandler<String, String> responseHandler) {
        RequestContext<String> context = new RequestContext<>("test", "method", timeOut);

        if (responseHandler == null) {
            responseHandler = new PassthroughResponseHandler<>();
        }
        TestRequest request = new TestRequest(null, context, responseHandler, getCallLogger());
        return request;
    }

    protected TestRequest exec(TestRequest request) {
        getExecutor().execute(request, request.getResponse());
        return request;
    }

    protected void verifyStatistics(int active, int queue, int completed, int rejected) {
        verifyStatistics(active, queue, Long.valueOf(completed), Long.valueOf(rejected));
    }

    protected void verifyStatistics(int active, int queue, long completed, long rejected) {
        QueueStatistic queueStatistic = getExecutor().getQueueStatistic();

        assertThat("Task count differs", queueStatistic.getActiveTaskCount(), is(active));
        assertThat("Queue size differs", queueStatistic.getQueueSize(), is(queue));
        assertThat("Completed task count differs", queueStatistic.getCompletedTaskCount(), is(completed));
        assertThat("Rejected count differs", queueStatistic.getRejectedTaskCount(), is(rejected));
    }

    protected void createExecutor(int coreSize, int maxSize, int queueSize) {
        executor = new HawaiiExecutorImpl("name", coreSize, maxSize, queueSize, new TimeOut(1, TimeUnit.MINUTES), logManager);
        executor.prestartAllCoreThreads();
    }

    protected Response<String> dispatch(final RequestDispatcher dispatcher, final AbstractAbortableRequest request) {
        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    dispatcher.execute(request);
                } catch (ServerException e) {
                    e.printStackTrace();
                }
            }
        };

        t.setDaemon(true);
        t.start();

        return request.getResponse();
    }

    protected Response<String> dispatchAsync(final RequestDispatcher dispatcher, final AbstractAbortableRequest request) {
        assert getExecutor().getMaximumPoolSize() > 1 : "For async calls pool size > 1 required!";

        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    dispatcher.executeAsync(request);
                } catch (ServerException e) {
                    e.printStackTrace();
                }
            }
        };

        t.setDaemon(true);
        t.start();

        return request.getResponse();
    }

}
