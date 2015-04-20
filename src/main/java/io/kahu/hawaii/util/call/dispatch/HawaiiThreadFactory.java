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

import java.util.concurrent.ThreadFactory;

public class HawaiiThreadFactory implements ThreadFactory {
    private final String threadPrefix;
    private int nrThreads;

    public HawaiiThreadFactory(String threadPrefix) {
        this.threadPrefix = threadPrefix;
    }

    @Override
    public Thread newThread(Runnable r) {
        synchronized (this) {
            nrThreads++;
        }
        Thread t = new Thread(r, threadPrefix + "-" + nrThreads);
        t.setDaemon(true);
        return t;
    }

}
