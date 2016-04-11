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
package io.kahu.hawaii.util.call;

import io.kahu.hawaii.util.call.dispatch.RequestConfiguration;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.http.annotation.ThreadSafe;

@ThreadSafe
public class RequestContext<T> {
    private final String backendSystem;
    private final String methodName;

    private RequestConfiguration configuration;

    // rejected and time out objects should be obtained from memory using the
    // context. This way the returned object is always the "latest" and not the
    // object created when the request was submitted.
    private T rejected;
    private T aborted;

    public RequestContext(String backendSystem, String methodName) {
        this.backendSystem = backendSystem;
        this.methodName = methodName;
        configuration = new RequestConfiguration();
    }

    public RequestContext(String backendSystem, String methodName, int timeOut) {
        this.backendSystem = backendSystem;
        this.methodName = methodName;
        configuration = new RequestConfiguration();
        configuration.setTimeOut(new TimeOut(timeOut, TimeUnit.SECONDS));
    }

    public RequestContext(String backendSystem, String methodName, TimeOut timeOut) {
        this.backendSystem = backendSystem;
        this.methodName = methodName;
        configuration = new RequestConfiguration();
        configuration.setTimeOut(timeOut);
    }

    public String getBackendSystem() {
        return backendSystem;
    }

    public String getMethodName() {
        return methodName;
    }

    public T getRejectResponse() {
        return rejected;
    }

    public void setRejectResponse(T rejected) {
        this.rejected = rejected;
    }

    public T getTimeOutResponse() {
        return aborted;
    }

    public void setAbortResponse(T aborted) {
        this.aborted = aborted;
    }

    public boolean is(String system, String method) {
        return this.backendSystem.equalsIgnoreCase(system) && this.methodName.equalsIgnoreCase(method);
    }

    @Override
    public String toString() {
        return getBackendSystem() + "." + getMethodName();
    }

    public RequestConfiguration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(RequestConfiguration configuration) {
        if (this.configuration == null) {
            this.configuration = configuration;
        } else {
            if (StringUtils.isNotBlank(configuration.getQueue())) {
                this.configuration.setQueue(configuration.getQueue());
            }
            if (configuration.getTimeOut() != null) {
                this.configuration.setTimeOut(configuration.getTimeOut());
            }
        }
    }

    public TimeOut getTimeOut() {
        return configuration.getTimeOutOrDefaultIfUnset();
    }

    public String getQueue() {
        return configuration.getQueue();
    }
}
