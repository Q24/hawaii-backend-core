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

import io.kahu.hawaii.util.exception.ServerException;
import org.jolokia.jmx.JsonMBean;

@JsonMBean
public class RuntimeFeatureMBean {

    private RuntimeFeaturesService runtimeFeaturesService;

    public RuntimeFeatureMBean(RuntimeFeaturesService runtimeFeaturesService) {
        this.runtimeFeaturesService = runtimeFeaturesService;
    }

    public String get(String name) throws ServerException {
        RuntimeFeatures features = runtimeFeaturesService.getRuntimeFeatures();
        RuntimeFeature feature = features.getFeature(name);
        if (feature == null) {
            throw new IllegalArgumentException("Invalid runtime feature: " + name);
        }
        return feature.getValue();
    }

    public void set(String name, String value) throws ServerException {
        RuntimeFeatures features = runtimeFeaturesService.getRuntimeFeatures();
        RuntimeFeature feature = features.getFeature(name);
        if (feature == null) {
            throw new IllegalArgumentException("Invalid runtime feature: " + name);
        }
        feature.setValue(value);
        runtimeFeaturesService.updateRuntimeFeatures(features);
    }
}
