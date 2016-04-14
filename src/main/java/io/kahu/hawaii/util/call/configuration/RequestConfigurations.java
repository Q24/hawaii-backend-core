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
package io.kahu.hawaii.util.call.configuration;

import io.kahu.hawaii.util.call.TimeOut;
import io.kahu.hawaii.util.call.http.HttpRequestContext;
import org.jolokia.jmx.JsonMBean;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@JsonMBean
public class RequestConfigurations {
    private final Map<String, RequestConfiguration> configurations = new HashMap<>();

    public RequestConfiguration get(String key) {
        RequestConfiguration configuration = configurations.get(key);
        if (configuration == null) {
            configuration = new RequestConfiguration();
            configurations.put(key, configuration);
        }
        return configuration;
    }

    public void setTimeOut(String key, int timeOut) {
        get(key).setTimeOut(new TimeOut(timeOut, TimeUnit.SECONDS));
    }

    public void setExecutorName(String key, String queue) {
        get(key).setExecutorName(queue);
    }

    public void changeUrl(String system, String method, String url) {
        String key = system + "." + method;
        RequestConfiguration configuration = get(key);
        HttpRequestContext context = (HttpRequestContext) configuration.getContext();
        context.setBaseUrl(url);
    }
}
