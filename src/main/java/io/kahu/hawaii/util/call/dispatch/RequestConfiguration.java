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

import java.util.concurrent.TimeUnit;

public class RequestConfiguration {
    private String queue = ExecutorServiceRepository.DEFAULT_POOL_NAME;
    private final Integer defaultTimeOut = 10;
    private Integer timeOut = null;

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public Integer getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(Integer timeOut) {
        this.timeOut = timeOut;
    }

    public TimeUnit getTimeOutUnit() {
        return TimeUnit.SECONDS;
    }

    public int getTimeOutOrDefaultIfUnset() {
        if (timeOut == null) {
            return defaultTimeOut;
        }
        return timeOut;
    }

}
