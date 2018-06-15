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
package io.kahu.hawaii.util.call.configuration;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import io.kahu.hawaii.util.call.RequestBuilder;
import io.kahu.hawaii.util.call.RequestContext;
import io.kahu.hawaii.util.call.TimeOut;
import io.kahu.hawaii.util.call.dispatch.ExecutorRepository;
import io.kahu.hawaii.util.call.dispatch.HawaiiExecutorImpl;
import io.kahu.hawaii.util.logger.CoreLoggers;
import io.kahu.hawaii.util.logger.LogManager;
import io.kahu.hawaii.util.spring.ApplicationContextProvider;

public class DispatcherConfigurator implements ApplicationListener<ContextRefreshedEvent> {
    private final LogManager logManager;
    private final ExecutorRepository executorServiceRepository;
    private final RequestConfigurations requestConfigurations;

    public DispatcherConfigurator(ExecutorRepository executorServiceRepository, RequestConfigurations requestConfigurations, LogManager logManager) {
        this.executorServiceRepository = executorServiceRepository;
        executorServiceRepository.setRequestConfigurations(requestConfigurations);

        this.requestConfigurations = requestConfigurations;

        this.logManager = logManager;
    }

    public void configure(File configFile) {
        try {
            String configuration = FileUtils.readFileToString(configFile);
            JSONObject json = new JSONObject(configuration);
            configure(json);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void configure(JSONObject json) throws JSONException {
        parseConfig(json);
    }

    private void parseConfig(JSONObject json) throws JSONException {
        Map<String, String> defaultExecutors = new HashMap<>();
        Map<String, HawaiiExecutorImpl> executors = new HashMap<>();

        JSONArray queues = json.getJSONArray("queues");
        for (int i = 0; i < queues.length(); i++) {
            JSONObject queue = queues.getJSONObject(i);
            String name = queue.getString("name");
            int corePoolSize = queue.getInt("core_pool_size");
            int maxPoolSize = queue.getInt("max_pool_size");
            int keepAliveTime = queue.getInt("keep_alive_time");
            int maxPendingRequests = queue.getInt("max_pending_requests");

            logManager.info(CoreLoggers.SERVER, "Creating queue '" + name + "' with '" + corePoolSize + "'/'" + maxPoolSize + "'/'" + maxPendingRequests + "'.");
            HawaiiExecutorImpl executor = new HawaiiExecutorImpl(name, corePoolSize, maxPoolSize, maxPendingRequests, new TimeOut(keepAliveTime, TimeUnit.SECONDS), logManager);

            executors.put(name, executor);
        }

        JSONArray systems = json.getJSONArray("systems");
        for (int i = 0; i < systems.length(); i++) {
            JSONObject system = systems.getJSONObject(i);
            String systemName = system.getString("name");
            Integer systemTimeOutValue = system.optInt("default_timeout", -1);
            TimeOut systemTimeOut;
            if (systemTimeOutValue > 0) {
                systemTimeOut = new TimeOut(systemTimeOutValue, TimeUnit.SECONDS);
                requestConfigurations.get(systemName).setTimeOut(systemTimeOut);
            }

            String defaultQueue = system.optString("default_queue");
            if (StringUtils.isNotBlank(defaultQueue)) {
                assert executors.containsKey(defaultQueue) : "The configured queue '" + defaultQueue + "' does not exist.";
                defaultExecutors.put(systemName, defaultQueue);
            }

            JSONArray calls = system.optJSONArray("calls");
            if (calls != null) {
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
                        configuration.setExecutorName(defaultQueue);
                    }
                    if (StringUtils.isNotBlank(queue)) {
                        configuration.setExecutorName(queue);
                    }

                    if (timeOut > 0) {
                        configuration.setTimeOut(new TimeOut(timeOut, TimeUnit.SECONDS));
                    }
                }
            }
        }

        executors.values().forEach(executorServiceRepository::add);

        executorServiceRepository.setDefaultExecutors(defaultExecutors);

        executorServiceRepository.configure();
    }

    private String createLookup(String system, String method) {
        return system + "." + method;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        Collection<RequestBuilder> beans = ApplicationContextProvider.getBeans(RequestBuilder.class);
        for (RequestBuilder builder : beans) {
            RequestContext requestContext = builder.getRequestContext();
            String lookup = createLookup(requestContext.getBackendSystem(), requestContext.getMethodName());

            RequestConfiguration requestConfiguration = requestConfigurations.get(lookup);
            requestContext.setConfiguration(requestConfiguration);
            requestConfiguration.setContext(requestContext);
            logManager.debug(CoreLoggers.SERVER,
                    "Configuring call '" + lookup + "' to use '" + requestContext.getExecutorName() + "' with timeout '" + requestContext.getTimeOut() + "'.");
        }
    }

}
