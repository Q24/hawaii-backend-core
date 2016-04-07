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
import io.kahu.hawaii.util.call.RequestContext;
import io.kahu.hawaii.util.call.Response;
import io.kahu.hawaii.util.call.ResponseStatus;
import io.kahu.hawaii.util.call.http.HttpCall;
import io.kahu.hawaii.util.call.statistics.QueueStatistic;
import io.kahu.hawaii.util.exception.ServerException;
import io.kahu.hawaii.util.logger.CoreLoggers;
import io.kahu.hawaii.util.logger.LogManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeoutException;

import io.kahu.hawaii.util.logger.LoggingContext;
import org.apache.http.impl.client.HttpClientBuilder;

public class RequestDispatcher {
    private final HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();

    private final LogManager logManager;
    private final ExecutorServiceRepository executorServiceRepository;

    private final List<RequestDispatchedListener> listeners = new ArrayList<>();

    /**
     * Note that the listeners added here will <em>all</em> be invoked for
     * <em>each</em> call. Each listener is invoked before the actual dispatch
     * and this is done <em<sequentially</em>.
     *
     * So please be very careful what listeners you register.
     *
     * @param executorServiceRepository
     * @param logManager
     * @param listeners
     */
    public RequestDispatcher(ExecutorServiceRepository executorServiceRepository, LogManager logManager, RequestDispatchedListener... listeners) {
        this.executorServiceRepository = executorServiceRepository;
        this.logManager = logManager;
        if (listeners != null) {
            for (RequestDispatchedListener listener : listeners) {
                this.listeners.add(listener);
            }
        }
        httpClientBuilder.disableContentCompression();
    }

    public <T> Set<Response<T>> execute(RequestFactory<T> requestFactory, boolean waitForAnswers) throws ServerException {
        Set<Response<T>> responses = new HashSet<>();
        CountDownLatch latch = new CountDownLatch(requestFactory.getNumberOfRequests());
        AbortableRequest<T> request = requestFactory.getNextRequest();
        while (request != null) {
            request.setLatch(latch);
            responses.add(executeAsync(request));
            request = requestFactory.getNextRequest();
        }

        if (waitForAnswers) {
            try {
                latch.await();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        return responses;
    }

    /**
     * Non blocking (asynchronous) execute of the request.
     *
     * @param request
     * @param <T>
     * @return
     * @throws ServerException
     */
    public <T> Response<T> executeAsync(AbortableRequest<T> request) throws ServerException {
        AsyncFutureRequest asyncFutureRequest = new AsyncFutureRequest(request, this);
        try {
            executorServiceRepository.getServiceMonitor(request).execute(asyncFutureRequest);
        } catch (RejectedExecutionException e) {
            request.reject();
            request.finish();
        }
        return request.getResponse();
    }

    /**
     * Blocking (synchronous) execute of the request.
     *
     * @param request
     * @param <T>
     * @return
     * @throws ServerException
     */
    public <T> Response<T> execute(AbortableRequest<T> request) throws ServerException {
        if (request instanceof HttpCall) {
            ((HttpCall) request).setHttpClientBuilder(httpClientBuilder);
        }

        RequestContext<T> requestContext = request.getContext();
        /*
         * Get the response here in order to return it.
         */
        Response<T> response = request.getResponse();

        try {
            HawaiiThreadPoolExecutor executor = executorServiceRepository.getService(request);

            QueueStatistic queueStatistics = executor.getQueueStatistic();
            request.getStatistic().setQueueStatistic(queueStatistics);

            notifyListeners(request, executor);

            FutureRequest<T> task = new FutureRequest<>(request, response);

            /*
             * TODO Move into requestLogger.
             */
            logScheduleStart(request, executor, queueStatistics);
            executor.execute(task);

            /*
             * Block until data is retrieved, but with a time out.
             */
            task.get(requestContext.getTimeOut(), requestContext.getTimeOutUnit());

        } catch (RejectedExecutionException e) {
            request.reject();
        } catch (InterruptedException e) {
            response.setStatus(ResponseStatus.INTERNAL_FAILURE, "Interrupted", e);
        } catch (ExecutionException e) {
            response.setStatus(ResponseStatus.INTERNAL_FAILURE, "Interrupted", e);
        } catch (TimeoutException e) {
            request.abort();
        } finally {
            request.finish();
        }

        return response;
    }

    private <T> void logScheduleStart(AbortableRequest<T> request, HawaiiThreadPoolExecutor executor, QueueStatistic queueStatistics) {
        try (LoggingContext.PopResource pushContext = logManager.pushContext()) {
            logManager.putContext("queue.name", executor.getName());

            logManager.putContext("pool.size.current", queueStatistics.getPoolSize());
            logManager.putContext("pool.size.max", queueStatistics.getMaximumPoolSize());
            logManager.putContext("pool.size.largest", queueStatistics.getLargestPoolSize());
            logManager.putContext("pool.task.pending", queueStatistics.getQueueSize());
            logManager.putContext("pool.task.active", queueStatistics.getActiveTaskCount());
            logManager.putContext("pool.task.completed", queueStatistics.getCompletedTaskCount());
            logManager.putContext("pool.task.rejected", queueStatistics.getRejectedTaskCount());

            logManager.info(CoreLoggers.SERVER, "Scheduling request '" + request.getCallName() + "' with id '" + request.getId() + "'.");
        }
    }

    private <T> void notifyListeners(AbortableRequest<T> request, HawaiiThreadPoolExecutor executor) {
        for (RequestDispatchedListener listener : listeners) {
            listener.notifyBeforeDispatch(request, request.isAsync(), executor);
        }
    }
}
