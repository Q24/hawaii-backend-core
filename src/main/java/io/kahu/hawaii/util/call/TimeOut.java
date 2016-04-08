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

import java.util.concurrent.TimeUnit;

public class TimeOut {
    private final int duration;
    private final TimeUnit unit;

    public TimeOut(int duration, TimeUnit unit) {
        this.duration = duration;
        this.unit = unit;
    }

    public int getDuration() {
        return duration;
    }

    public TimeUnit getUnit() {
        return unit;
    }
}
