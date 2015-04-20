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

import java.util.HashMap;
import java.util.Map;

import org.jolokia.jmx.JsonMBean;

@JsonMBean
public class RequestConfigurations {
    private final Map<String, RequestConfiguration> configurations = new HashMap<String, RequestConfiguration>();

    public RequestConfiguration get(String key) {
        RequestConfiguration configuration = configurations.get(key);
        if (configuration == null) {
            configuration = new RequestConfiguration();
            configurations.put(key, configuration);
        }
        return configuration;
    }

    public void setTimeOut(String key, int timeOut) {
        get(key).setTimeOut(timeOut);
    }

    public void setQueue(String key, String queue) {
        get(key).setQueue(queue);
    }
}
