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
package io.kahu.hawaii.util.call.statistics;

import io.kahu.hawaii.util.call.ResponseStatus;

public class RequestStatistic {
    private long startNano;
    private long startCallNano;
    private long endCallNano;
    private long startConversionNano;
    private long endConversionNano;
    private long startCallbackNano;
    private long endCallbackNano;
    private long endNano;

    private QueueStatistic queueStatistic;

    private ResponseStatus status;

    public void startRequest() {
        this.startNano = System.nanoTime();
    }

    public void endRequest() {
        this.endNano = System.nanoTime();
    }

    public void startBackendRequest() {
        this.startCallNano = System.nanoTime();
    }

    public void endBackendRequest() {
        this.endCallNano = System.nanoTime();
    }

    public void startCallback() {
        this.startCallbackNano = System.nanoTime();
    }

    public void endCallback() {
        this.endCallbackNano = System.nanoTime();
    }

    public void startConversion() {
        startConversionNano = System.nanoTime();
    }

    public void endConversion() {
        endConversionNano = System.nanoTime();
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("\tTotal Duration '" + getTotalDuration() + "' msec.\n");
        builder.append("\tQueue time     '" + getQueueTime() + "' msec.\n");
        builder.append("\tCall time      '" + getCallTime() + "' msec.\n");
        builder.append("\tConversion     '" + getConversionDuration() + "' msec.\n");
        builder.append("\tCallback       '" + getCallbackDuration() + "' msec.\n");
        builder.append("\tStatus         '" + status + "'.\n");
        return builder.toString();
    }

    private double diff(long start, long end) {
        if (end == 0) {
            return 0.0D;
        }
        return (end - start) / 1E6;
    }

    public void setStatus(ResponseStatus status) {
        this.status = status;
    }

    public double getTotalDuration() {
        return diff(startNano, endNano);
    }

    public double getQueueTime() {
        return diff(startNano, startCallNano);
    }

    public double getCallTime() {
        return diff(startCallNano, endCallNano);
    }

    public double getConversionDuration() {
        return diff(startConversionNano, endConversionNano);
    }

    public double getCallbackDuration() {
        return diff(startCallbackNano, endCallbackNano);
    }

    public void setQueueStatistic(QueueStatistic queueStatistic) {
        this.queueStatistic = queueStatistic;
    }

    public QueueStatistic getQueueStatistic() {
        return queueStatistic;
    }

}
