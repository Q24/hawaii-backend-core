/**
 * Copyright 2014-2018 Q24
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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.annotation.ThreadSafe;

import io.kahu.hawaii.util.call.AbortableRequest;
import io.kahu.hawaii.util.call.RequestContext;
import io.kahu.hawaii.util.call.configuration.RequestConfiguration;
import io.kahu.hawaii.util.call.configuration.RequestConfigurations;
import io.kahu.hawaii.util.logger.LogManager;

@ThreadSafe
public class ExecutorRepository {
    private final Map<String, String> defaultExecutors = new HashMap<>();
    private final Map<String, HawaiiExecutor> executors = new HashMap<>();
    private RequestConfigurations requestConfigurations;

    public static final String DEFAULT_EXECUTOR_NAME = "default";
    public static final String DEFAULT_ASYNC_EXECUTOR_NAME = "async_executor_guard";

    private final LogManager logManager;

    public ExecutorRepository(final LogManager logManager) {
        this.logManager = logManager;
    }

    public void setRequestConfigurations(RequestConfigurations requestConfigurations) {
        this.requestConfigurations = requestConfigurations;
    }

    public void add(HawaiiExecutor executor) {
        this.executors.put(executor.getName(), executor);
    }

    public void addDefaultExecutor(String systemName, String executorName) {
        assert executors.containsKey(executorName) : "The queue with name '" + executorName + "' is not defined.";
        defaultExecutors.put(systemName, executorName);
    }

    public void setDefaultExecutors(Map<String, String> defaultExecutors) {
        this.defaultExecutors.clear();
        for (Entry<String, String> entry : defaultExecutors.entrySet()) {
            addDefaultExecutor(entry.getKey(), entry.getValue());
        }
    }

    public void configure() {
        assert executors.containsKey(DEFAULT_EXECUTOR_NAME) : "The system queue with name '" + DEFAULT_EXECUTOR_NAME + "' is not defined.";
        assert executors.containsKey(DEFAULT_ASYNC_EXECUTOR_NAME) : "The system queue with name '" + DEFAULT_ASYNC_EXECUTOR_NAME + "' is not defined.";
    }

    public HawaiiExecutor getExecutorByName(final String name) {
        return executors.get(name);
    }

    public <T> HawaiiExecutor getExecutor(AbortableRequest<T> request) {
        RequestContext<T> context = request.getContext();

        HawaiiExecutor executor = executors.get(context.getExecutorName());
        if (executor == null) {
            // The configured (in the request) executor does not exist (or is empty).
            // Get the external configuration and add the configuration to the request
            RequestConfiguration configuration = requestConfigurations.get(request.getCallName());
            executor = executors.get(configuration.getExecutorName());

            if (executor == null) {
                // See if there is a default executor for the backend system.
                String executorName = defaultExecutors.get(context.getBackendSystem());
                executor = executors.get(executorName);

                if (executor == null) {
                    // Use the trusty old default pool.
                    executor = executors.get(DEFAULT_EXECUTOR_NAME);
                }

            }

            configuration.setExecutorName(executor.getName());
            request.getContext().setConfiguration(configuration);
        }

        return executor;
    }

    public <T> HawaiiExecutor getAsyncExecutor(AbortableRequest<T> request) {
        return executors.get(DEFAULT_ASYNC_EXECUTOR_NAME);
    }

    public void stop() {
        for (HawaiiExecutor executor : executors.values()) {
            executor.shutdown();
        }
    }
}
