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
package io.kahu.hawaii.util.spring;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

public class HandlerMethodAdapter implements ApplicationContextAware, InitializingBean {

    private ApplicationContext applicationContext;
    Map<String, List<HandlerMethodMappingInfo>> mapping;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, List<HandlerMethodMappingInfo>> mapping = new HashMap<>();
        RequestMappingHandlerMapping handlerMapping = applicationContext.getBean(RequestMappingHandlerMapping.class);
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMapping.getHandlerMethods().entrySet()) {
            HandlerMethod handlerMethod = entry.getValue();
            String controllerClassName = handlerMethod.getBeanType().getName();
            if (!mapping.containsKey(controllerClassName)) {
                mapping.put(controllerClassName, new ArrayList<HandlerMethodMappingInfo>());
            }
            mapping.get(controllerClassName).add(new HandlerMethodMappingInfo(entry.getKey(), entry.getValue()));
        }
        this.mapping = mapping;
    }

    public Map<String, List<HandlerMethodMappingInfo>> getEligibleHandlerMethods(HandlerMethodFilter... handlerMethodFilters) {
        Map<String, List<HandlerMethodMappingInfo>> result = new HashMap<>();
        if (handlerMethodFilters == null || handlerMethodFilters.length == 0) {
            result.putAll(mapping);
        } else {
            for (Map.Entry<String, List<HandlerMethodMappingInfo>> entry : mapping.entrySet()) {
                List<HandlerMethodMappingInfo> eligibleMappingInfo = new ArrayList<>();
                for (HandlerMethodMappingInfo handlerMethodMappingInfo : entry.getValue()) {
                    if (isEligible(handlerMethodMappingInfo.getHandlerMethod(), handlerMethodFilters)) {
                        eligibleMappingInfo.add(handlerMethodMappingInfo);
                    }
                }
                if (CollectionUtils.isNotEmpty(eligibleMappingInfo)) {
                    result.put(entry.getKey(), eligibleMappingInfo);
                }
            }
        }
        return result;
    }

    private boolean isEligible(HandlerMethod handlerMethod, HandlerMethodFilter... handlerMethodFilters) {
        boolean result = true;
        for (HandlerMethodFilter handlerMethodFilter : handlerMethodFilters) {
            result &= handlerMethodFilter.isEligible(handlerMethod);
        }
        return result;
    }

}
