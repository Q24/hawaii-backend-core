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

import io.kahu.hawaii.util.call.TimeOut;
import org.apache.http.annotation.NotThreadSafe;

import java.util.concurrent.TimeUnit;

@NotThreadSafe
public class RequestConfiguration {
    private String queue = ExecutorServiceRepository.DEFAULT_POOL_NAME;
    private TimeOut defaultTimeOut = new TimeOut(10, TimeUnit.SECONDS);
    private TimeOut timeOut = null;

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public TimeOut getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(TimeOut timeOut) {
        this.timeOut = timeOut;
    }

    public TimeOut getTimeOutOrDefaultIfUnset() {
        if (timeOut == null) {
            return defaultTimeOut;
        }
        return timeOut;
    }

}
