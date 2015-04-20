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

import io.kahu.hawaii.util.call.AbortableRequest;
import io.kahu.hawaii.util.call.Response;
import io.kahu.hawaii.util.logger.LoggingContext;

import java.util.concurrent.Callable;

public class CallableRequest<T> implements Callable<Response<T>> {
    private final AbortableRequest<T> abortableRequest;

    public CallableRequest(AbortableRequest<T> abortableRequest) {
        this.abortableRequest = abortableRequest;
    }

    @Override
    public Response<T> call() throws Exception {
        LoggingContext.remove();
        Response<T> response = abortableRequest.getResponse();
        try {
            abortableRequest.doExecute();
            abortableRequest.doCallback();
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } catch (Throwable t) {
            throw new Exception(t);
        } finally {
            abortableRequest.logResponse();
            LoggingContext.remove();
        }
    }

    public void abort() {
        abortableRequest.abort();
    }
}
