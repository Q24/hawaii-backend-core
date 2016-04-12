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
import io.kahu.hawaii.util.exception.ServerException;
import io.kahu.hawaii.util.logger.LogManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

import org.apache.http.annotation.ThreadSafe;

@ThreadSafe
public class RequestDispatcher {
    private final LogManager logManager;
    private final ExecutorRepository executorServiceRepository;

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
    public RequestDispatcher(ExecutorRepository executorServiceRepository, LogManager logManager, RequestDispatchedListener... listeners) {
        this.executorServiceRepository = executorServiceRepository;
        this.logManager = logManager;
        if (listeners != null) {
            for (RequestDispatchedListener listener : listeners) {
                this.listeners.add(listener);
            }
        }
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
        try {
            executorServiceRepository.getAsyncExecutor(request).executeAsync(request, this);
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
        Response<T> response = request.getResponse();

        try {
            HawaiiExecutor executor = executorServiceRepository.getExecutor(request);

            notifyListeners(request, executor);


            FutureTask<T> task = executor.execute(request, response);

            /*
             * Block until data is retrieved, but with a time out.
             */
            TimeOut timeOut = request.getTimeOut();
            task.get(timeOut.getDuration(), timeOut.getUnit());

        } catch (RejectedExecutionException e) {
            // Executor is too busy (no threads available nor is there a place in the queue).
            request.reject();
        } catch (TimeoutException e) {
            // The task.get( ... ) is timed out. The execution takes too long.
            request.abort();
        } catch (InterruptedException e) {
            // ..
            response.setStatus(ResponseStatus.INTERNAL_FAILURE, "Interrupted", e);
        } catch (ExecutionException e) {
            // Catches all exceptions from within the executor
            response.setStatus(ResponseStatus.INTERNAL_FAILURE, "Execution exception", e.getCause());
        } catch (Throwable t) {
            // Catches all exceptions outside the executor
            response.setStatus(ResponseStatus.INTERNAL_FAILURE, "Unexpected exception", t);
        } finally {
            request.finish();
        }

        return response;
    }



    private <T> void notifyListeners(AbortableRequest<T> request, HawaiiExecutor executor) {
        for (RequestDispatchedListener listener : listeners) {
            listener.notifyBeforeDispatch(request, request.isAsync(), executor);
        }
    }
}
