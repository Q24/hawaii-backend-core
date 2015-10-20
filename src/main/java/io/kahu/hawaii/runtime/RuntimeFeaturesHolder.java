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
package io.kahu.hawaii.runtime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.util.Assert;

public class RuntimeFeaturesHolder implements Serializable {

    private Map<String, RuntimeFeature> lookupMap;
    private Map<String, Object> featuresMap;

    public RuntimeFeaturesHolder(Map<String, Object> featuresMap) {
        this.featuresMap = featuresMap;
        buildLookupMap();
    }

    public RuntimeFeature getRuntimeFeature(String name) {
        return lookupMap.get(name);
    }

    public List<RuntimeFeature> getRuntimeFeatures() {
        return getRuntimeFeatures(null);
    }

    public List<RuntimeFeature> getRuntimeFeatures(List<String> exclude) {
        if (exclude == null) {
            return new ArrayList<>(lookupMap.values());
        }
        return lookupMap.keySet().stream().filter(key -> !exclude.contains(key)).map(key -> lookupMap.get(key)).collect(Collectors.toList());
    }

    public void setRuntimeFeature(RuntimeFeature runtimeFeature) {
        List<String> keys = Arrays.asList(runtimeFeature.getName().split("\\."));
        Map<String, Object> map = featuresMap;
        for (Iterator<String> iterator = keys.iterator(); iterator.hasNext(); ) {
            String key = iterator.next();
            if (iterator.hasNext()) {
                map = (Map) map.get(key);
            } else {
                map.put(key, runtimeFeature);
            }
        }
        lookupMap.put(runtimeFeature.getName(), runtimeFeature);
    }

    private void buildLookupMap() {
        Map<String, RuntimeFeature> result = new HashMap<>();
        process(featuresMap, null, result);
        lookupMap = result;
    }

    private void process(Map<String, Object> source, String key, Map<String, RuntimeFeature> result) {
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String newKey = key == null ? entry.getKey() : key + '.' + entry.getKey();
            if (entry.getValue() instanceof Map) {
                process((Map) entry.getValue(), newKey, result);
            } else if (entry.getValue() instanceof RuntimeFeature) {
                String storedKey = ((RuntimeFeature) entry.getValue()).getName();
                Assert.isTrue(newKey.equals(storedKey), "Resolved and stored key are different! '" + newKey + "' != '" + storedKey + "'");
                result.put(newKey, (RuntimeFeature) entry.getValue());
            } else {
                throw new IllegalArgumentException("Unsupported class: " + entry.getValue().getClass().getName());
            }
        }
    }

}
