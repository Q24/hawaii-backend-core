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
import io.kahu.hawaii.util.logger.LogManager;
import org.apache.http.annotation.ThreadSafe;
import org.codehaus.jettison.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@ThreadSafe
public class ExecutorServiceRepository {
    private final Map<String, String> defaultExecutors = new HashMap<>();
    private final Map<String, HawaiiThreadPoolExecutor> executors = new HashMap<>();

    public static final String DEFAULT_POOL_NAME = "default";
    public static final String DEFAULT_ASYNC_POOL_NAME = "async_executor_guard";

    private final LogManager logManager;

    public ExecutorServiceRepository(final LogManager logManager) {
        this.logManager = logManager;
    }

    /**
     * Deprecated, use dispatcher configurator.
     *
     * @param configFile
     * @param logManager
     * @param requestConfigurations
     * @throws IOException
     * @throws JSONException
     */
    @Deprecated
    public ExecutorServiceRepository(File configFile, final LogManager logManager, RequestConfigurations requestConfigurations) throws IOException,
            JSONException {
        this.logManager = logManager;
        new DispatcherConfigurator(configFile, this, requestConfigurations, logManager);
    }

    public void add(HawaiiThreadPoolExecutor executor) {
        this.executors.put(executor.getName(), executor);
    }

    public void setDefaultExecutors(Map<String, String> defaultExecutors) {
        this.defaultExecutors.clear();
        this.defaultExecutors.putAll(defaultExecutors);
    }

    public void configure() {
        assert this.executors.containsKey(DEFAULT_POOL_NAME) : "The system queue with name '" + DEFAULT_POOL_NAME + "' is not defined.";
        assert this.executors.containsKey(DEFAULT_ASYNC_POOL_NAME) : "The system queue with name '" + DEFAULT_ASYNC_POOL_NAME + "' is not defined.";
    }


    private <T> HawaiiThreadPoolExecutor getQueue(AbortableRequest<T> request) {
        HawaiiThreadPoolExecutor service = null;

        RequestContext<T> context = request.getContext();

        service = executors.get(context.getQueue());
        if (service == null) {
            String executorName = defaultExecutors.get(context.getBackendSystem());
            service = executors.get(executorName);

            if (service == null) {
                service = executors.get(DEFAULT_POOL_NAME);
            }

            if (context.getQueue() == null) {
                context.getConfiguration().setQueue(service.getName());
            }
        }


        return service;
    }

    public <T> HawaiiThreadPoolExecutor getService(AbortableRequest<T> request) {
        return getQueue(request);
    }

    public <T> HawaiiThreadPoolExecutor getServiceMonitor(AbortableRequest<T> request) {
        return executors.get(DEFAULT_ASYNC_POOL_NAME);
    }

    public void stop() {
        for (HawaiiThreadPoolExecutor executor : executors.values()) {
            executor.shutdown();
        }
    }
}
