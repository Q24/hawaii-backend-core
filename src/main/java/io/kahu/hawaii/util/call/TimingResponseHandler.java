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

import io.kahu.hawaii.util.call.statistics.RequestStatistic;
import io.kahu.hawaii.util.exception.ServerException;

public class TimingResponseHandler<F, T> implements ResponseHandler<F, T> {
    private final ResponseHandler<F, T> delegate;
    private final RequestStatistic statistic;

    public TimingResponseHandler(ResponseHandler<F, T> delegate, RequestStatistic statistic) {
        this.delegate = delegate;
        this.statistic = statistic;
    }

    @Override
    public void addToResponse(F payload, Response<T> response) throws ServerException {
        statistic.startConversion();
        try {
            delegate.addToResponse(payload, response);
        } finally {
            statistic.endConversion();
        }
    }
}
