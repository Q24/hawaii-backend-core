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
import io.kahu.hawaii.util.exception.ServerException;
import io.kahu.hawaii.util.logger.LogManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeoutException;

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
     * So please be very carefull what listeners you register.
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
        Set<Response<T>> responses = new HashSet<Response<T>>();
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

    public <T> Response<T> execute(AbortableRequest<T> request) throws ServerException {
        Response<T> response = execute(request, true);
        return response;
    }

    public <T> Response<T> executeAsync(AbortableRequest<T> request) throws ServerException {
        Response<T> response = execute(request, false);
        return response;
    }

    private <T> Response<T> execute(AbortableRequest<T> request, boolean synchronous) throws ServerException {
        if (request instanceof HttpCall) {
            ((HttpCall) request).setHttpClientBuilder(httpClientBuilder);
        }
        RequestContext<T> requestContext = request.getContext();
        Response<T> response = request.getResponse();

        FutureRequest<T> task = new FutureRequest<T>(request);
        try {
            HawaiiThreadPoolExecutor executor = executorServiceRepository.getService(request);
            notifyListeners(request, synchronous, executor);
            executor.execute(task);

            if (synchronous) {
                /*
                 * Block until data is retrieved, but with a time out.
                 */
                task.get(requestContext.getTimeOut(), requestContext.getTimeOutUnit());
                request.logResponse();
            } else {
                /*
                 * Schedule a new task to verify the
                 */
                AsyncRequestTimeoutFutureTask<T> guardTask = new AsyncRequestTimeoutFutureTask<T>(task, logManager);
                request.setGuardTask(guardTask);

                executorServiceRepository.getServiceMonitor(request).execute(guardTask);
            }
        } catch (RejectedExecutionException e) {
            request.setTooBusy();
            request.logResponse();
        } catch (InterruptedException e) {
            response.setStatus(ResponseStatus.INTERNAL_FAILURE, "Interrupted", e);
            request.logResponse();
        } catch (ExecutionException e) {
            response.setStatus(ResponseStatus.INTERNAL_FAILURE, "Interrupted", e);
            request.logResponse();
        } catch (TimeoutException e) {
            request.abort();
            request.logResponse();
        }

        request.finish();
        return response;
    }

    private <T> void notifyListeners(AbortableRequest<T> request, boolean synchronous, HawaiiThreadPoolExecutor executor) {
        for (RequestDispatchedListener listener : listeners) {
            listener.notifyBeforeDispatch(request, synchronous, executor);
        }
    }
}
