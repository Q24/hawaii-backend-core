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
import io.kahu.hawaii.util.call.http.HttpRequestBuilder;
import io.kahu.hawaii.util.call.statistics.QueueStatistic;
import io.kahu.hawaii.util.logger.CoreLoggers;
import io.kahu.hawaii.util.logger.LogManager;
import io.kahu.hawaii.util.logger.LoggingContext.PopResource;
import io.kahu.hawaii.util.spring.ApplicationContextProvider;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

public class ExecutorServiceRepository implements ApplicationListener<ContextRefreshedEvent> {
    private final RequestConfigurations requestConfigurations;
    private final Map<String, String> defaultExecutors = new HashMap<String, String>();
    private final Map<String, HawaiiThreadPoolExecutor> executors = new HashMap<String, HawaiiThreadPoolExecutor>();

    static final String DEFAULT_POOL_NAME = "default";
    private static final String GUARD_POOL_NAME = "async_executor_guard";

    private final LogManager logManager;

    public ExecutorServiceRepository(File configFile, final LogManager logManager, RequestConfigurations requestConfigurations) throws IOException,
            JSONException {
        this.logManager = logManager;
        this.requestConfigurations = requestConfigurations;

        String configuration = FileUtils.readFileToString(configFile);
        JSONObject json = new JSONObject(configuration);
        parseConfig(json);

        assert executors.containsKey(DEFAULT_POOL_NAME) : "The system queue with name '" + DEFAULT_POOL_NAME + "' is not defined.";
        assert executors.containsKey(GUARD_POOL_NAME) : "The system queue with name '" + GUARD_POOL_NAME + "' is not defined.";
    }

    private void parseConfig(JSONObject json) throws JSONException {
        RejectedExecutionHandler handler = new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
                logManager.info(CoreLoggers.SERVER, "Rejected '" + ((FutureRequest<?>) runnable).getAbortableRequest()
                        + "' since the pool and queue size has been exceeded.");
                throw new RejectedExecutionException();
            }
        };

        JSONArray queues = json.getJSONArray("queues");
        for (int i = 0; i < queues.length(); i++) {
            JSONObject queue = queues.getJSONObject(i);
            String name = queue.getString("name");
            int corePoolSize = queue.getInt("core_pool_size");
            int maxPoolSize = queue.getInt("max_pool_size");
            int keepAliveTime = queue.getInt("keep_alive_time");
            int maxPendingRequests = queue.getInt("max_pending_requests");

            logManager
                    .info(CoreLoggers.SERVER, "Creating queue '" + name + "' with '" + corePoolSize + "'/'" + maxPoolSize + "'/'" + maxPendingRequests + "'.");
            HawaiiThreadPoolExecutorImpl executor = new HawaiiThreadPoolExecutorImpl(name, corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS,
                    new ArrayBlockingQueue<Runnable>(maxPendingRequests), new HawaiiThreadFactory(name), handler);

            executors.put(name, executor);
        }

        JSONArray systems = json.getJSONArray("systems");
        for (int i = 0; i < systems.length(); i++) {
            JSONObject system = systems.getJSONObject(i);
            String systemName = system.getString("name");

            String defaultQueue = system.optString("default_queue");
            if (StringUtils.isNotBlank(defaultQueue)) {
                assert executors.containsKey(defaultQueue) : "The configured queue '" + defaultQueue + "' does not exist.";
                defaultExecutors.put(systemName, defaultQueue);
            }

            JSONArray calls = system.getJSONArray("calls");
            for (int j = 0; j < calls.length(); j++) {
                JSONObject call = calls.getJSONObject(j);
                String method = call.getString("method");
                Integer timeOut = call.optInt("time_out", -1);
                String queue = call.optString("queue");
                if (StringUtils.isNotBlank(queue)) {
                    assert executors.containsKey(queue) : "The configured queue '" + queue + "' does not exist.";
                }

                String lookup = createLookup(systemName, method);
                RequestConfiguration configuration = requestConfigurations.get(lookup);
                if (StringUtils.isNotBlank(defaultQueue)) {
                    configuration.setQueue(defaultQueue);
                }
                if (StringUtils.isNotBlank(queue)) {
                    configuration.setQueue(queue);
                }

                if (timeOut > 0) {
                    configuration.setTimeOut(timeOut);
                }
            }
        }
    }

    public <T> HawaiiThreadPoolExecutor getQueue(AbortableRequest<T> request) {
        RequestContext<T> context = request.getContext();

        String executorName = context.getQueue();
        HawaiiThreadPoolExecutor service = executors.get(executorName);
        if (service == null) {
            service = executors.get(DEFAULT_POOL_NAME);
        }
        return service;
    }

    public <T> HawaiiThreadPoolExecutor getService(AbortableRequest<T> request) {
        HawaiiThreadPoolExecutor service = getQueue(request);

        QueueStatistic queueStatistics = service.getQueueStatistic();
        request.getStatistic().setQueueStatistic(queueStatistics);

        try (PopResource pushContext = logManager.pushContext()) {
            logManager.putContext("queue.name", service.getName());

            logManager.putContext("pool.size.current", queueStatistics.getPoolSize());
            logManager.putContext("pool.size.max", queueStatistics.getMaximumPoolSize());
            logManager.putContext("pool.size.largest", queueStatistics.getLargestPoolSize());
            logManager.putContext("pool.task.pending", queueStatistics.getQueueSize());
            logManager.putContext("pool.task.active", queueStatistics.getActiveTaskCount());
            logManager.putContext("pool.task.completed", queueStatistics.getCompletedTaskCount());
            logManager.putContext("pool.task.rejected", queueStatistics.getRejectedTaskCount());

            logManager.info(CoreLoggers.SERVER, "Scheduling request '" + request + "' with id '" + request.getId() + "'.");
        }

        return service;
    }

    private String createLookup(String system, String method) {
        return system + "." + method;
    }

    public <T> ExecutorService getServiceMonitor(AbortableRequest<T> request) {
        return executors.get(GUARD_POOL_NAME);
    }

    public void stop() {
        for (HawaiiThreadPoolExecutor executor : executors.values()) {
            executor.shutdown();
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        Collection<HttpRequestBuilder> beans = ApplicationContextProvider.getBeans(HttpRequestBuilder.class);
        for (HttpRequestBuilder builder : beans) {
            RequestContext requestContext = builder.getRequestContext();
            String lookup = createLookup(requestContext.getBackendSystem(), requestContext.getMethodName());

            RequestConfiguration requestConfiguration = requestConfigurations.get(lookup);
            requestContext.setConfiguration(requestConfiguration);
            logManager.debug(CoreLoggers.SERVER,
                    "Configuring call '" + lookup + "' to use '" + requestContext.getQueue() + "' with timeout '" + requestContext.getTimeOut() + "'.");
        }
    }
}
