/**
 * Copyright 2014-2018 Q24
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

import io.kahu.hawaii.util.call.AbortableRequest;
import io.kahu.hawaii.util.call.Response;
import io.kahu.hawaii.util.logger.LoggingContext;
import org.apache.http.annotation.NotThreadSafe;

import java.util.concurrent.Callable;

@NotThreadSafe
public class AsyncCallableRequest<T> implements Callable<Response<T>> {
    private final AbortableRequest<T> abortableRequest;
    private final RequestDispatcher requestDispatcher;

    public AsyncCallableRequest(AbortableRequest<T> abortableRequest, RequestDispatcher requestDispatcher) {
        this.abortableRequest = abortableRequest;
        this.requestDispatcher = requestDispatcher;
    }

    @Override
    public Response<T> call() throws Exception {
        return requestDispatcher.execute(abortableRequest);
    }

}
